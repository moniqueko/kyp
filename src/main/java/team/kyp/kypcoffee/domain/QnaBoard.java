package team.kyp.kypcoffee.domain;

import lombok.Data;

import java.util.Date;

@Data
public class QnaBoard {
    private Integer qnaBoardNum;
    private Integer memberNum;
    private String memberName;
    private String qnaBoardTitle;
    private String qnaBoardContent;
    private Date qnaBoardDate;

    public QnaBoard(Integer qnaBoardNum, Integer memberNum, String qnaBoardTitle, String qnaBoardContent, Date qnaBoardDate) {
        this.qnaBoardNum = qnaBoardNum;
        this.memberNum = memberNum;
        this.qnaBoardTitle = qnaBoardTitle;
        this.qnaBoardContent = qnaBoardContent;
        this.qnaBoardDate = qnaBoardDate;
    }



}
