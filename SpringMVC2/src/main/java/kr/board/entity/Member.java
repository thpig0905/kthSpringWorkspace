package kr.board.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Member {
	  private int memIdx; 
	  private String memID;  
	  private String memPassword;
	  private String memName;
	  private int memAge; // <-null, 0
	  private String memGender;
	  private String memEmail;
	  private String memProfile; //사진정보
	  
	  
	  public boolean nullValueCheck() {
		  if(memID == null || memID.equals("") ) return false;
		  if(memPassword == null || memPassword.equals("") ) return false;
		  if(memName == null || memName.equals("") ) return false;
		  if(memAge != 0 ) return false;
		  if(memGender == null || memGender.equals("") ) return false;
		  if(memEmail == null || memEmail.equals("") ) return false;
		  return true;
	  }

	  
}
