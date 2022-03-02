package com.web.doitcommit.dto.board;

import com.web.doitcommit.domain.board.Board;
import com.web.doitcommit.domain.boardCategory.BoardCategory;
import com.web.doitcommit.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.NotBlank;

@Schema(description = "게시글 등록 dto")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardRegDto {

    @Schema(description = "카테고리 아이디")
    @NotBlank
    private Long categoryId;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "글제목")
    @NotBlank
    private String boardTitle;

    @Schema(description = "내용")
    @NotBlank
    private String boardContent;

    public Board toEntity(Long principalId){
        Board board = Board.builder()
                .boardCategory(BoardCategory.builder().categoryId(categoryId).build())
                .member(Member.builder().memberId(principalId).build())
                .boardTitle(boardTitle)
                .boardContent(boardContent)
                .build();
        return board;
    }
}
