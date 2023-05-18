package com.studygroup.service.groupmember;

import com.studygroup.entity.StudyGroup;
import com.studygroup.entity.StudyGroupMember;
import com.studygroup.exception.CustomIllegalArgumentException;
import com.studygroup.repository.GroupMemberRepository;
import com.studygroup.util.constant.ErrorCode;
import com.studygroup.util.lambda.BindParameterSupplier;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RetrieveGroupMemberByNickNameAndGroupServiceImpl implements RetrieveGroupMemberByNickNameAndGroupService {
    private final GroupMemberRepository groupMemberRepository;
    @Override
    public StudyGroupMember get(StudyGroup studyGroup, String nickName) {
        return Optional.
                    ofNullable(groupMemberRepository.
                            findByStudyGroupAndNickName(studyGroup, nickName)).
                orElseThrow(BindParameterSupplier.
                        bind(CustomIllegalArgumentException::new,
                                ErrorCode.GROUP_MEMBER_IS_NOT_EXISTED));

    }
}
