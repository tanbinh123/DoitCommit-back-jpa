package com.web.doitcommit.domain.board;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.web.doitcommit.domain.hashtag.QBoardHashtag;
import com.web.doitcommit.domain.hashtag.QTagCategory;
import org.springframework.util.CollectionUtils;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.web.doitcommit.domain.board.QBoard.board;

public class BoardRepositoryImpl implements BoardRepositoryQuerydsl {

    private final JPAQueryFactory queryFactory;

    public BoardRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    /**
     * 게시판 목록 조회
     */
    @Override
    public List<Board> getCustomBoardList(int pageNo, int pageSize) {
        List<Long> ids = queryFactory
                .select(board.boardId)
                .from(board)
                .orderBy(board.boardId.desc())
                .limit(pageSize)
                .offset(pageNo * pageSize)
                .fetch();

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        return queryFactory
                .select(board)
                .from(board)
                .where(board.boardId.in(ids))
                .orderBy(board.boardId.desc())
                .fetch();
    }

    /**
     * 게시글의 태그 목록 조회
     * @return
     */
    @Override
    public List getCustomTagList(Long boardId) {
        QTagCategory tagCategory = QTagCategory.tagCategory;
        QBoardHashtag boardHashtag = QBoardHashtag.boardHashtag;

        List<Tuple> results = queryFactory
                .select(tagCategory.tagId, tagCategory.tagName)
                .from(tagCategory)
                .leftJoin(boardHashtag).on(tagCategory.tagId.eq(boardHashtag.tagCategory.tagId))
                .where(boardHashtag.board.boardId.eq(boardId))
                .fetch();

        
        List tagList = new ArrayList();
        for (Tuple result : results) {
            Map<Long,String> tagMap = new HashMap<>();
            tagMap.put(result.get(tagCategory.tagId), result.get(tagCategory.tagName));
            tagList.add(tagMap);
        }

        return tagList;
    }
}
