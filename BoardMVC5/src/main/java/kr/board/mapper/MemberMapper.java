package kr.board.mapper;

import kr.board.entity.AuthVO;
import kr.board.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

	public Member memLogin(String username);
	public Member registerCheck(String memID);
	public int register(Member member);
	public int memUpdate(Member member);
	public Member getMember(String memID);
	public int memProfileUpdate(Member member);
	public void authInsert(AuthVO auto);
	public void authDelete(String memID);
	
	
}
