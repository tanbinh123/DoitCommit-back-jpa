package com.web.doitcommit.service.member;

import com.web.doitcommit.domain.member.Member;
import com.web.doitcommit.domain.member.MemberRepository;
import com.web.doitcommit.dto.member.MemberInfoDto;
import com.web.doitcommit.dto.member.MemberUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Value("${file.path}")
    private String uploadFolder;

    public MemberInfoDto reqGetMemberInfo(Long memberId) {
        Member member = memberRepository.findByMemberId(memberId);
        MemberInfoDto memberInfo = new MemberInfoDto(member);
        return memberInfo;
    }

    public Boolean reqGetMemberCheck(String nickname) {
        int count = memberRepository.mNicknameCount(nickname);
        if (count > 0) {
            return false;
        }
        return true;
    }

    @Transactional
    public Boolean reqPutMemberUpdate(MemberUpdateDto memberUpdateDto, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않은 회원입니다."));

        MultipartFile file = memberUpdateDto.getFile();
        if (file != null) {
            String path = "D:\\doitcommit\\upload\\"; //폴더 경로 // Windows('\'), Linux, MAC('/')

            //파일 업로드 utill로 리팩토링 예정
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String str = sdf.format(date); //오늘날짜를 포맷함
            String datePath = str.replace("-", File.separator);

            //폴더생성
            File uploadPath = new File(path, datePath);
            if (!uploadPath.exists()) {
                try {
                    uploadPath.mkdirs();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }

            UUID uuid = UUID.randomUUID();
            String imageFileName = path + datePath + File.separator + uuid + "_" + memberUpdateDto.getFile().getOriginalFilename();

            Path imageFilePath = Paths.get(imageFileName);
            member.changePictureUrl(imageFileName);

            try {
                Files.write(imageFilePath, file.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        member.changeNickname(memberUpdateDto.getNickname());
        member.changeEmail(memberUpdateDto.getEmail());
        member.changeInterestTechSet(memberUpdateDto.getInterestTechSet());
        member.changeGithubUrl(memberUpdateDto.getGithubUrl());
        member.changeUrl1(memberUpdateDto.getUrl1());
        member.changeUrl2(memberUpdateDto.getUrl2());

        return true;
    }


}
