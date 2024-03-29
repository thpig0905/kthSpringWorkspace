package kr.board.controller;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import kr.board.entity.AuthVO;
import kr.board.entity.Member;
import kr.board.entity.MemberUser;
import kr.board.mapper.MemberMapper;
import kr.board.security.MemberUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RequestMapping("/member")
@Controller
public class MemberController {
	
	@Autowired
	MemberMapper memberMapper;
	
	@Autowired
	PasswordEncoder pwEncoder; // 암호화 할 수있는 객체 
	
	@Autowired
	MemberUserDetailsService memberUserDetailsService;
	
	@ModelAttribute("cp")
	public String getContextPath(Model model , HttpServletRequest request) {		
		model.addAttribute("cp", request.getContextPath());	
		return request.getContextPath();
	}
	
	@GetMapping("/memLoginForm.do")
	public String loginForm() {
		return "member/memLoginForm";
	}
	
	@GetMapping("/memUpdateForm.do")
	public String UpdateForm() {
		return "member/memUpdateForm";
	}

	@GetMapping("/memJoin.do")
	public String memberJoin() {
		return "member/join";
	}
	
	@PostMapping("/memRegister.do") //@ModelAttribute        // @RequestParam(value="memPassword1")
	public String registerMember( Member m , String memPassword1 , String memPassword2, 
			RedirectAttributes rttr , HttpSession session 
			) {
		System.out.println("m = " + m );
		System.out.println(" === memRegister ===  ");
		if(!m.nullValueCheck()) {
			rttr.addFlashAttribute("msgType" ,"실패 메세지");
			rttr.addFlashAttribute("msg" ,"모든 값을 넣어주세요 ");
			return "redirect:/member/memJoin.do";
		}
		if(!memPassword1.equals(memPassword2)) {
			rttr.addFlashAttribute("msgType" ,"실패 메세지");
			rttr.addFlashAttribute("msg" ,"패스워드 값이 서로 다릅니다 ");
			return "redirect:/member/memJoin.do";
		}
		
		m.setMemPassword(memPassword1); // 암호화 전 페스워드 
		m.setMemProfile(""); // 사진이 없다는 의미 
		
		String encyptPw= pwEncoder.encode(m.getMemPassword());
		System.out.println("encyptPw = " + encyptPw);
		
		// 암호화한 패스워드 다시 객체에 넣기 
		m.setMemPassword(encyptPw);
		
		// 멤버회원 추가 됨 
		int result = memberMapper.register(m);

		if(result == 1) {
			
			// 멤버 생성 후 권한 테이블 생성 
			List<AuthVO> list = m.getAuthList();
			for(AuthVO vo : list) {
				if(vo.getAuth() != null ) {
					AuthVO auth = new AuthVO();
					auth.setMemID(m.getMemID());
					auth.setAuth(vo.getAuth()); // ROLE_USER, ROLE_ADMIN.. 
					System.out.println("auth = " + auth);
					memberMapper.authInsert(auth);
				}
			}
			
			rttr.addFlashAttribute("msgType" ,"성공 메세지");
			rttr.addFlashAttribute("msg" ,"회원가입 성공했습니다 ");
			// session.setAttribute("mvo", m);
			
			return "redirect:/member/memLoginForm.do";
		}else {
			rttr.addFlashAttribute("msgType" ,"실패 메세지");
			rttr.addFlashAttribute("msg" ," 회원가입 실패 다시시도해주세요 ");
			return "redirect:/member/memJoin.do";
		}
	
	}
	
	
	@GetMapping("/memRegisterCheck.do")
	public @ResponseBody int memRegisterCheck( String memID ) {
		System.out.println("memRegisterCheck memId = " + memID );
		Member member = memberMapper.registerCheck(memID);
		
		return member == null ? 1 : 0;
	}
	
	// 회원 정보 수정 
	@PostMapping("/memUpdate.do")  // @ModelAttribute  new Member(), setter
	public String memUpdate( @ModelAttribute Member m, RedirectAttributes rttr ,
		                      @RequestParam String memPassword1, String memPassword2, HttpSession session) {

		if(!m.nullValueCheck()) {
			rttr.addFlashAttribute("msgType" ,"실패 메세지");
			rttr.addFlashAttribute("msg" ,"모든 값을 넣어주세요 ");
			return "redirect:/member/memUpdateForm.do";
		}
		
		if(!memPassword1.equals(memPassword2)) {
			rttr.addFlashAttribute("msgType" ,"실패 메세지");
			rttr.addFlashAttribute("msg" ,"패스워드 값이 서로 다릅니다 ");
			return "redirect:/member/memUpdateForm.do";
		}
		
		m.setMemPassword(memPassword2);
		System.out.println("update m = " + m );
		
		// 비번 암호화 
		m.setMemPassword(pwEncoder.encode(m.getMemPassword()));
		
		int result = memberMapper.memUpdate(m);
		if(result == 1) {
			rttr.addFlashAttribute("msgType" ,"성공 메세지");
			rttr.addFlashAttribute("msg" ,"회원 정보 수정완료  ");
			
			// 기존 권한 다 삭제 
			memberMapper.authDelete(m.getMemID());
			
			// 다시 새로운 권한 추가 
			List<AuthVO> list = m.getAuthList();
			for(AuthVO vo : list) {
				if(vo.getAuth() != null ) {
					AuthVO auth = new AuthVO();
					auth.setMemID(m.getMemID());
					auth.setAuth(vo.getAuth()); // ROLE_USER, ROLE_ADMIN.. 
					memberMapper.authInsert(auth);
				}
			}
			
			// 회원 정보업데이트 된 세션으로 재등록 
		//	session.setAttribute("mvo", m);
			
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			MemberUser userAccount = (MemberUser)authentication.getPrincipal();
			SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(authentication, userAccount.getMember().getMemID()));
			
			
			return "redirect:/";
		}else {
			rttr.addFlashAttribute("msgType" ,"실패 메세지");
			rttr.addFlashAttribute("msg" ,"회원 정보 수정실패  ");
			return "redirect:/member/memUpdateForm.do";
		}

	}
	
	// 회원 사진 등록 
	@GetMapping("/memImageForm.do")
	public String memImageForm() {
		return "/member/memImageForm";
	}
	
	
	@PostMapping("/memImageUpdate.do")
	public String memImageUpdate(HttpServletRequest request,HttpSession session, RedirectAttributes rttr) {
		MultipartRequest multi = null;
		int fileMaxSize = 10*1024*1024; // 10MB
		String savePath = request.getSession().getServletContext().getRealPath("resources/upload");
		Path uploadDirectory = Paths.get(savePath);
		int result = 0;
		if(!Files.exists(uploadDirectory)) { // 업로드 폴더 없으면 생성 
			try {
				Files.createDirectory(uploadDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// 이미지 업로드 
		try {
			multi = new MultipartRequest(request, savePath, fileMaxSize, "UTF-8" , new DefaultFileRenamePolicy());
			
			String memID = multi.getParameter("memID");
			Member mvo = memberMapper.getMember(memID);
			if(mvo == null) {
				return "redirect:/";
			}
	
			
			File file = multi.getFile("memProfile");
			if(file.exists()) {
				System.out.println("저장완료 ");
				System.out.println("저장 경로 " + savePath);
				
				
				String ext = file.getName().substring(file.getName().lastIndexOf(".")+1);
				ext = ext.toUpperCase(); // png, PNG, jpg, JPG,
					
				// 이미지 확장자 아니면 되돌아가기 
				if(!(ext.equals("PNG") || ext.equals("JPG"))){
					
					rttr.addFlashAttribute("msgType" ,"실패 메세지");
					rttr.addFlashAttribute("msg" ," 이미지 사진만 업로드 가능합니다  ");
					return "redirect:/member/memImageForm.do";
					
				}
				
				String newProfile =file.getName(); // 현재 업로드한 파일 이름  
				String oldProfile= mvo.getMemProfile(); // 기존 이미지 파일 이름 
				
				// 기존에 이미지 파일이 있다면 삭제 
				File oldFile = new File(savePath +"/" + oldProfile);
				
				if(oldFile.exists()) {
					oldFile.delete();
				}
				
				mvo.setMemProfile(newProfile);
				result = memberMapper.memProfileUpdate(mvo);
				System.out.println("이미지 업로드 mvo = " + mvo );
				
			}
			
			// db 이미지 업로드 성공 후
			if(result == 1) {
				//session.setAttribute("mvo", mvo);
				
                // 새로운 인증 세션을 생성 
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				MemberUser userAccount = (MemberUser)authentication.getPrincipal();
				SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(authentication, userAccount.getMember().getMemID()));
				
				rttr.addFlashAttribute("msgType" ,"성공 메세지");
				rttr.addFlashAttribute("msg" ," 이미지 등록 성공  ");
				return "redirect:/";
				
			}
	
		} catch (IOException e) {

			e.printStackTrace();
		}

		
		return "redirect:/";
	}
	
    protected Authentication createNewAuthentication(Authentication currentAuth, String username) {
        UserDetails newPrincipal = memberUserDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(newPrincipal, currentAuth.getCredentials(), newPrincipal.getAuthorities());
        newAuth.setDetails(currentAuth.getDetails());        
        return newAuth;
 }
	
	
	
	
	
	
	
	
}
