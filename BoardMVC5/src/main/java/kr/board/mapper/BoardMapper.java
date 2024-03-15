package kr.board.mapper;

import kr.board.entity.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper  // 기본생성자 -> setter()
public interface BoardMapper {
	public List<Board> getLists();
	public int boardInsert(Board board);
	public Board boardContent(int idx);
	public int boardDelete(int idx);
	public int boardUpdate(Board board);
    @Update("update myboard set count=count+1 where idx=#{idx}")
    public int boardCount(int idx);
 }
