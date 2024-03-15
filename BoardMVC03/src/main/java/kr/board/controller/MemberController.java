package kr.board.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.board.entity.Member;
import kr.board.mapper.MemberMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

// mvc2
@RequestMapping("/member")
@Controller
public class MemberController {

    @Autowired
    MemberMapper memberMapper;

    @ModelAttribute("cp")
    public String getContextPath(Model model, HttpServletRequest request) {
        model.addAttribute("cp", request.getContextPath());
        return request.getContextPath();
    }

    @GetMapping("/memJoin.do")
    public String memJoin() {
        return "member/join";  // join.jsp
    }

    @RequestMapping("/memRegisterCheck.do")
    public @ResponseBody int memRegisterCheck(String memID) {
        System.out.println("test");

        Member m = memberMapper.registerCheck(memID);
        if (m != null || memID.equals("")) {
            return 0; //이미 존재하는 회원, 입력불가
        }
        return 1; //사용가능한 아이디
    }

    // 회원가입 처리
    @RequestMapping("/memRegister.do")
    public String memRegister(Member m, String memPassword1, String memPassword2,
                              RedirectAttributes rttr, HttpSession session) {
        System.out.println("==== memRegister.do ====");

        if (m.nullValueCheck()) {
            // 누락메세지를 가지고 가기? =>객체바인딩(Model, HttpServletRequest, HttpSession)
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "모든 내용을 입력하세요.");
            return "redirect:/member/memJoin.do";  // ${msgType} , ${msg}
        }
        if (!memPassword1.equals(memPassword2)) {
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "비밀번호가 서로 다릅니다.");
            return "redirect:/member/memJoin.do";  // ${msgType} , ${msg}
        }
        m.setMemProfile(""); // 사진이미는 없다는 의미 ""
        m.setMemPassword(memPassword1);
        // 회원을 테이블에 저장하기
        int result = memberMapper.register(m);
        if (result == 1) { // 회원가입 성공 메세지
            rttr.addFlashAttribute("msgType", "성공 메세지");
            rttr.addFlashAttribute("msg", "회원가입에 성공했습니다.");
            // 회원가입이 성공하면=>로그인처리하기
            session.setAttribute("mvo", m); // ${!empty mvo}
            return "redirect:/";
        } else {
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "이미 존재하는 회원입니다.");
            return "redirect:/member/memJoin.do";
        }
    }

    // 로그아웃 처리
    @RequestMapping("/memLogout.do")
    public String memLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 로그인 화면으로 이동
    @RequestMapping("/memLoginForm.do")
    public String memLoginForm() {
        return "member/memLoginForm"; // memLoginForm.jsp
    }

    // 로그인 기능 구현
    @RequestMapping("/memLogin.do")
    public String memLogin(Member m, RedirectAttributes rttr, HttpSession session) {
        if (m.getMemID() == null || m.getMemID().equals("") ||
                m.getMemPassword() == null || m.getMemPassword().equals("")) {
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "모든 내용을 입력해주세요.");
            return "redirect:/memLoginForm.do";
        }
        Member mvo = memberMapper.memLogin(m);
        if (mvo != null) { // 로그인에 성공
            rttr.addFlashAttribute("msgType", "성공 메세지");
            rttr.addFlashAttribute("msg", "로그인에 성공했습니다.");
            session.setAttribute("mvo", mvo); // ${!empty mvo}
            return "redirect:/";     // 메인
        } else { // 로그인에 실패
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "다시 로그인 해주세요.");
            return "redirect:/member/memLoginForm.do";
        }
    }

    // 회원정보수정화면
    @RequestMapping("/memUpdateForm.do")
    public String memUpdateForm() {
        return "member/memUpdateForm";
    }

    // 회원정보수정
    @RequestMapping("/memUpdate.do")
    public String memUpdate(Member m, RedirectAttributes rttr,
                            String memPassword1, String memPassword2, HttpSession session) {

        if (m.nullValueCheck()) {
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "모든 내용을 입력하세요.");
            return "redirect:/member/memJoin.do";  // ${msgType} , ${msg}
        }
        if (!memPassword1.equals(memPassword2)) {
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "비밀번호가 서로 다릅니다.");
            return "redirect:/member/memJoin.do";  // ${msgType} , ${msg}
        }
        m.setMemProfile(""); // 사진은 없다는 의미 ""
        m.setMemPassword(memPassword1);
        int result = memberMapper.memUpdate(m);

        if (result == 1) {
            rttr.addFlashAttribute("msgType", "성공 메세지");
            rttr.addFlashAttribute("msg", "회원정보 수정에 성공했습니다.");
            session.setAttribute("mvo", m); // ${!empty mvo}
            return "redirect:/";
        } else {
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "회원정보 수정에 실패했습니다.");
            return "redirect:/member/memUpdateForm.do";
        }
    }

    @GetMapping("/memImageForm.do")
    public String memImageForm() {
        return "member/memImageForm";
    }

    @PostMapping("/memImageUpdate.do")
    public String memImageUpdate(@RequestParam("memProfile") MultipartFile file, HttpSession session,
                                 RedirectAttributes rttr, HttpServletRequest request) {
        if (file.isEmpty()) {
            // 파일이 비어있을 경우 처리
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "파일을 선택해주세요.");
            return "redirect:/member/memImageForm.do";
        }

        String memID = request.getParameter("memID"); // 직접 파라미터 가져오기

        // 파일 업로드 처리
        try {
            String fileName = file.getOriginalFilename();
            String uploadDir = request.getSession().getServletContext().getRealPath("/resources/upload");
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // 업로드 성공 처리
            rttr.addFlashAttribute("msgType", "성공 메세지");
            rttr.addFlashAttribute("msg", "파일 업로드에 성공했습니다.");
        } catch (IOException e) {
            // 업로드 실패 처리
            e.printStackTrace();
            rttr.addFlashAttribute("msgType", "실패 메세지");
            rttr.addFlashAttribute("msg", "파일 업로드에 실패했습니다.");
        }

        // 기존 파일 삭제 처리
        Member mvo2 = (Member) session.getAttribute("mvo");
        String oldFileName = mvo2.getMemProfile();
        String uploadDir = request.getSession().getServletContext().getRealPath("/resources/upload");
        Path uploadPath = Paths.get(uploadDir);
        Path oldFilePath = uploadPath.resolve(oldFileName);
        if (Files.exists(oldFilePath)) {
            try {
                Files.delete(oldFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // memProfile 업데이트 처리
        Member m = new Member();
        m.setMemID(memID);
        m.setMemProfile(file.getOriginalFilename());
        int result = memberMapper.memProfileUpdate(m);

        // 업데이트 후 mvo에도 반영
        Member mvo = (Member) session.getAttribute("mvo");
        mvo.setMemProfile(file.getOriginalFilename());
        session.setAttribute("mvo", mvo);

        return "redirect:/";
    }
}
