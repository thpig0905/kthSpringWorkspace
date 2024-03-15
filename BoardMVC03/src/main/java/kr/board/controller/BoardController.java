package kr.board.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.board.entity.Board;
import kr.board.mapper.BoardMapper;


@RequestMapping("/board")
@RestController // @ResponseBody(JSON)응답
public class BoardController {

//	@RequestMapping("/boardMain.do")
//	public String main() {
//		return "board/main"; 
//	}

    @Autowired
    BoardMapper boardMapper;

    // @ResponseBody->jackson-databind(객체를->JSON 데이터포멧으로 변환)
    @GetMapping("/all")
    public List<Board> boardList() {
        List<Board> list = boardMapper.getLists();
        return list; // JSON 데이터 형식으로 변환(API)해서 리턴(응답)하겠다.
    }
    //@RequestMapping("/boardInsert.do")

//		@PostMapping("/boards")
//		public String addBoard(@RequestBody Board board) {
//			int result = boardMapper.boardInsert(board);
//			return (result == 1 ? "게시글 추가 완료" : "게시글 추가 실패") + board.toString();
//		}

    @PostMapping("/new")
    public String boardInsert(Board vo) {

        //System.out.println("vo = " + vo );
        int result = boardMapper.boardInsert(vo); // 등록성공

        return (result == 1 ? "게시글 등록 성공" : "게시글 등록 실패") + vo.toString();
    }

    @DeleteMapping("/{idx}")
    public String boardDelete(@PathVariable("idx") int idx) {
        int result = boardMapper.boardDelete(idx);

        return (result == 1 ? "게시글 삭제 성공" : "게시글 삭제 실패");
    }

    @PutMapping("/update")
    public String boardUpdate(@RequestBody Board vo) {
        int result = boardMapper.boardUpdate(vo);

        return (result == 1 ? "게시글 수정 성공" : "게시글 수정 실패") + vo.toString();
    }

    @GetMapping("/{idx}")
    public Board boardContent(@PathVariable("idx") int idx) {
        Board vo = boardMapper.boardContent(idx);
        return vo; // vo->JSON
    }

    @PutMapping("/count/{idx}")
    public Board boardCount(@PathVariable("idx") int idx) {
        boardMapper.boardCount(idx);
        Board vo = boardMapper.boardContent(idx);
        return vo;
    }

}
