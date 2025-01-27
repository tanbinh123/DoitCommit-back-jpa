package com.web.doitcommit.service.comment;

import com.web.doitcommit.domain.board.Board;
import com.web.doitcommit.domain.board.BoardRepository;
import com.web.doitcommit.domain.boardCategory.BoardCategory;
import com.web.doitcommit.domain.boardCategory.BoardCategoryRepository;
import com.web.doitcommit.domain.comment.Comment;
import com.web.doitcommit.domain.comment.CommentRepository;

import com.web.doitcommit.domain.comment.MemberTag;
import com.web.doitcommit.domain.comment.MemberTagRepository;
import com.web.doitcommit.domain.files.MemberImage;
import com.web.doitcommit.domain.files.MemberImageRepository;
import com.web.doitcommit.domain.hashtag.BoardHashtag;
import com.web.doitcommit.domain.hashtag.BoardHashtagRepository;
import com.web.doitcommit.domain.hashtag.TagCategory;
import com.web.doitcommit.domain.hashtag.TagCategoryRepository;
import com.web.doitcommit.domain.member.AuthProvider;
import com.web.doitcommit.domain.member.Member;
import com.web.doitcommit.domain.member.MemberRepository;
import com.web.doitcommit.dto.comment.CommentRegDto;
import com.web.doitcommit.dto.comment.CommentUpdateDto;
import com.web.doitcommit.dto.memberTag.MemberTagResDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class CommentServiceImplTest {

    @Autowired CommentService commentService;
    @Autowired MemberRepository memberRepository;
    @Autowired BoardRepository boardRepository;
    @Autowired BoardCategoryRepository boardCategoryRepository;
    @Autowired MemberTagRepository memberTagRepository;
    @Autowired CommentRepository commentRepository;
    @Autowired MemberImageRepository memberImageRepository;
    @Autowired TagCategoryRepository tagCategoryRepository;
    @Autowired BoardHashtagRepository boardHashtagRepository;

    private Member member;
    private Board board;
    private Set<Long> memberIdSet;

    @BeforeEach
    void before(){
        Member member = createMember("before@naver.com", "beforeNickname", "beforeUsername", "beforeOAuthId");
        this.member = member;

        BoardCategory category = createBoardCategory("testName");

        //게시글 생성
        Board board = createBoard(member, category, "testTitle", "testContent");
        this.board = board;

        //회원 태그 리스트
        Set<Long> memberIdSet = new HashSet<>();

        IntStream.rangeClosed(1,3).forEach(i -> {
            Member newMember = createMember("before" + i + "@naver.com", "beforeNickname" + i,
                    "beforeUsername" + i, "beforeOAuthId" + i);

            memberIdSet.add(memberRepository.save(newMember).getMemberId());
        });

        this.memberIdSet = memberIdSet;
    }


    @Test
    void 댓글작성() throws Exception{
        //given

        CommentRegDto commentRegDto =
                createCommentRegDto(board.getBoardId(), "testContent", memberIdSet);

        //when
        Comment comment = commentService.register(commentRegDto, member.getMemberId());

        //then
        assertThat(comment.getCommentId()).isNotNull();
        assertThat(comment.getBoard().getBoardId()).isEqualTo(board.getBoardId());
        assertThat(comment.getMember().getMemberId()).isEqualTo(member.getMemberId());
        assertThat(comment.getContent()).isEqualTo(commentRegDto.getContent());
        assertThat(comment.getIsExist()).isTrue();

        //memberTag
        Set<MemberTag> findMemberTagSet = memberTagRepository.findByComment(comment);
      
        assertThat(comment.getMemberTagSet()).isEqualTo(findMemberTagSet);
    }


    @Test
    void 댓글수정() throws Exception{
        //given
        Set<MemberTag> memberTagSet = new HashSet<>();

        memberIdSet.forEach(id ->{
            //tagMember 생성
            MemberTag memberTag = MemberTag.builder()
                    .member(Member.builder().memberId(id).build())
                    .build();

            //tagMember 추가
            memberTagSet.add(memberTag);
        });

        //댓글 생성
        Comment comment = createComment(member, board, "testContent", memberTagSet);

        //회원 태그할 회원 생성
        Set<Long> updateMemberIdSet = new HashSet<>();

        IntStream.rangeClosed(1,4).forEach(i -> {
            Member newMember = createMember("updateTest" + i + "@naver.com", "updateTestNickname" + i,
                    "updateTestUsername" + i, "updateTestOAuthId" + i);

            updateMemberIdSet.add(memberRepository.save(newMember).getMemberId());
        });


        CommentUpdateDto commentUpdateDto =
                createUpdateDto(comment.getCommentId(), "updateContent", updateMemberIdSet);

        //when
        commentService.modify(commentUpdateDto);

        //then
        Comment findComment = commentRepository.findById(comment.getCommentId()).get();
        Assertions.assertThat(findComment.getContent()).isEqualTo(commentUpdateDto.getContent());
        Assertions.assertThat(findComment.getMemberTagSet().size()).isEqualTo(4);

        System.out.println(findComment);
    }

    @Test
    void 댓글삭제() throws Exception{
        //given
        Comment comment = createComment(member, board, "testContent");

        //when
        Long removedCommentId = commentService.remove(comment.getCommentId());

        //then
        Comment removedComment = commentRepository.findById(removedCommentId).get();
        assertThat(removedComment.getCommentId()).isNotNull();
        assertThat(removedComment.getIsExist()).isFalse();
    }

    /**
     * board 1
     * comment 2 - 게시글 작성자 1(프로필사진x), 일반 회원 1 (프로필사진o)
     */
    @Test
    void 회원태그_리스트_조회 () throws Exception {
        //given
        //게시글 작성자 댓글 생성
        createComment(member, board, "testContent");

        //일반 회원 생성
        Member createMember = createMember("testMember@naver.com", "testNickname", "testUsername",
                "adf111");
        //일반 회원 프로필 사진 생성
        MemberImage createMemberImage = createMemberImage(createMember, "testFilePath", "testFileNm");
        //일반 회원 댓글 생성
        createComment(createMember, board, "testContent");


        //when
        List<MemberTagResDto> memberTagResList = commentService.getMemberTagList(board.getBoardId());

        //then
        assertThat(memberTagResList.size()).isEqualTo(2);

        assertThat(memberTagResList.get(0).getId()).isEqualTo(member.getMemberId().toString());
        assertThat(memberTagResList.get(0).getDisplay()).isEqualTo(member.getNickname());
        assertThat(memberTagResList.get(0).getImageUrl()).isNull();

        assertThat(memberTagResList.get(1).getId()).isEqualTo(createMember.getMemberId().toString());
        assertThat(memberTagResList.get(1).getDisplay()).isEqualTo(createMember.getNickname());
        assertThat(memberTagResList.get(1).getImageUrl()).isNotNull();
        assertThat(memberTagResList.get(1).getImageUrl()).isNotNull();

        for (MemberTagResDto memberTagResDto : memberTagResList) {
            System.out.println(memberTagResDto);
        }
    }

    private TagCategory createTagCategory(String tagName) {

        TagCategory tagCategory = TagCategory.builder().tagName(tagName).build();

        return tagCategoryRepository.save(tagCategory);
    }

    private MemberImage createMemberImage (Member member, String filePath, String testFileNm){
        MemberImage memberImage = new MemberImage(member, filePath, testFileNm);
        return memberImageRepository.save(memberImage);
    }

    private Comment createComment (Member member, Board board, String content){

        Comment comment = Comment.builder()
                .member(member)
                .board(board)
                .content(content)
                .build();
        return commentRepository.save(comment);
    }


    private Comment createComment (Member member, Board board, String content, Set<MemberTag> memberTagSet){

        Comment comment = Comment.builder()
                .member(member)
                .board(board)
                .content(content)
                .build();
        memberTagSet.forEach(memberTag -> comment.addMemberTag(memberTag));

        return commentRepository.save(comment);
    }

    private CommentUpdateDto createUpdateDto (Long commentId, String content, Set<Long> updateMemberIdSet){

        return CommentUpdateDto.builder()
                .commentId(commentId)
                .content(content)
                .memberIdSet(updateMemberIdSet)
                .build();
    }


    private CommentRegDto createCommentRegDto (Long boardId, String content, Set < Long > memberTagIdSet){

        return CommentRegDto.builder()
                .boardId(boardId)
                .content(content)
                .memberIdSet(memberTagIdSet)
                .build();
    }

    private Board createBoard (Member member, BoardCategory boardCategory, String title,
                               String content){
        Board board = Board.builder()
                .member(member)
                .boardCategory(boardCategory)
                .boardTitle(title)
                .boardContent(content)
                .build();

        boardRepository.save(board);

        return board;
    }

    private BoardCategory createBoardCategory (String categoryName){
        BoardCategory category = BoardCategory.builder().categoryName(categoryName).build();

        return boardCategoryRepository.save(category);
    }

    private Member createMember (String email, String nickname, String username, String oAuthId){
        Member member = Member.builder()
                .email(email)
                .nickname(nickname)
                .password("1111")
                .username(username)
                .provider(AuthProvider.GOOGLE)
                .interestTechSet(new HashSet<>(Arrays.asList("java")))
                .oauthId(oAuthId)
                .build();

        return memberRepository.save(member);
    }
}