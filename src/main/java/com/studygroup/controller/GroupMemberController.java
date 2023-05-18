package com.studygroup.controller;

import com.studygroup.dto.GroupApplicationForm;
import com.studygroup.dto.GroupApplicationList;
import com.studygroup.entity.Member;
import com.studygroup.entity.StudyGroup;
import com.studygroup.entity.StudyGroupMember;
import com.studygroup.enums.GroupMemberApplicationConSentOrDeny;
import com.studygroup.enums.GroupRole;
import com.studygroup.exception.ApiError;
import com.studygroup.service.groupmember.CheckGroupMemberIsPendingService;
import com.studygroup.service.HandleGroupMemberApplicationService;
import com.studygroup.service.group.RetrieveGroupByNameService;
import com.studygroup.service.groupmember.*;
import com.studygroup.service.user.RetrieveMemberByIdService;
import com.studygroup.util.constant.ErrorCode;
import com.studygroup.util.constant.ObjectToLong;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class GroupMemberController {

    private GroupMemberRemovalService kickGroupMember;
    private GroupMemberRemovalService groupMemberWithdrawalMemberSelf;
    private final CheckMemberIsAlreadyAppliedService checkMemberIsAlreadyAppliedService;
    private final CheckGroupMemberIsAlreadyDenied checkGroupMemberIsAlreadyDenied;
    private final CheckGroupMemberIsPendingService checkGroupMemberIsPending;
    private final TakeOverGroupAdminService takeOverGroupAdminService;
    private final HandleGroupMemberApplicationService handleGroupMemberApplicationService;
    private final RetrieveGroupMemberByMemberIdAndGroup retrieveGroupMemberByMemberIdAndGroup;
    private final RetrieveGroupMemberByNickNameAndGroupService retrieveGroupMemberByNickNameAndGroupService;
    private final RetrieveGroupMembersApplicationService retrieveGroupMembersApplication;
    private final RetrieveMemberByIdService retrieveMemberByIdService;
    private final ApplyTheGroupService applyTheGroupService;
    private final CheckGroupMemberNickNameIsDuplicatedService checkGroupMemberNickNameIsDuplicatedService;
    private final RetrieveGroupByNameService retrieveGroupByNameService;

    public GroupMemberController(@Qualifier("KickGroupMemberAsAdminService") GroupMemberRemovalService kickGroupMember,
                                 @Qualifier("GroupMemberIsOutMemberSelfService") GroupMemberRemovalService groupMemberWithdrawalMemberSelf,
                                 CheckMemberIsAlreadyAppliedService checkMemberIsAlreadyAppliedService,
                                 CheckGroupMemberIsAlreadyDenied checkGroupMemberIsAlreadyDenied,
                                 @Qualifier("CheckGroupMemberIsPendingServiceImpl") CheckGroupMemberIsPendingService checkGroupMemberIsPending,
                                 TakeOverGroupAdminService takeOverGroupAdminService,
                                 HandleGroupMemberApplicationService handleGroupMemberApplicationService,
                                 RetrieveGroupMemberByMemberIdAndGroup retrieveGroupMemberByMemberIdAndGroup,
                                 RetrieveGroupMemberByNickNameAndGroupService retrieveGroupMemberByNickNameAndGroupService,
                                 @Qualifier("RetrieveGroupMembersApplicationServiceImpl") RetrieveGroupMembersApplicationService retrieveGroupMembersApplication,
                                 RetrieveMemberByIdService retrieveMemberByIdService,
                                 @Qualifier("ApplyTheGroupServiceAsMember") ApplyTheGroupService applyTheGroupService,
                                 CheckGroupMemberNickNameIsDuplicatedService checkGroupMemberNickNameIsDuplicatedService,
                                 RetrieveGroupByNameService retrieveGroupByNameService) {
        this.kickGroupMember = kickGroupMember;
        this.groupMemberWithdrawalMemberSelf = groupMemberWithdrawalMemberSelf;
        this.checkMemberIsAlreadyAppliedService = checkMemberIsAlreadyAppliedService;
        this.checkGroupMemberIsAlreadyDenied = checkGroupMemberIsAlreadyDenied;
        this.checkGroupMemberIsPending = checkGroupMemberIsPending;
        this.takeOverGroupAdminService = takeOverGroupAdminService;
        this.handleGroupMemberApplicationService = handleGroupMemberApplicationService;
        this.retrieveGroupMemberByMemberIdAndGroup = retrieveGroupMemberByMemberIdAndGroup;
        this.retrieveGroupMemberByNickNameAndGroupService = retrieveGroupMemberByNickNameAndGroupService;
        this.retrieveGroupMembersApplication = retrieveGroupMembersApplication;
        this.retrieveMemberByIdService = retrieveMemberByIdService;
        this.applyTheGroupService = applyTheGroupService;
        this.checkGroupMemberNickNameIsDuplicatedService = checkGroupMemberNickNameIsDuplicatedService;
        this.retrieveGroupByNameService = retrieveGroupByNameService;
    }

    @PostMapping("/api/users/groups/{groupName}")
    public ResponseEntity<Object> userApplyTheGroup(@PathVariable String groupName,
                                                    @Valid @RequestBody GroupApplicationForm groupApplicationForm) {

        //JwtUtil parse the token and set the Authentication principle as memberId
        Object memberId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberLongId = ObjectToLong.convert(memberId);
        StudyGroup studyGroup = retrieveGroupByNameService.find(groupName);

        if (checkMemberIsAlreadyAppliedService.isAlreadyApplied(studyGroup, memberLongId)) {
            StudyGroupMember studyGroupMember = retrieveGroupMemberByMemberIdAndGroup.get(studyGroup, memberLongId);
            if (checkGroupMemberIsAlreadyDenied.isAlreadyDenied(studyGroupMember)) {
                return ApiError.buildApiError(
                        ErrorCode.YOU_ARE_DENIED_FROM_GROUP_ADMIN,
                        HttpStatus.FORBIDDEN);
            } else if (checkGroupMemberIsPending.isAlreadyApplied(studyGroupMember)) {
                return ApiError.buildApiError(
                        ErrorCode.YOU_ARE_NOW_PENDING_STATUS,
                        HttpStatus.FORBIDDEN);
            }

        }
            if (!checkGroupMemberNickNameIsDuplicatedService.
                    isDuplicated(studyGroup, groupApplicationForm.getNickName())) {
                Member member = retrieveMemberByIdService.getMember(ObjectToLong.convert(memberId));
                applyTheGroupService.apply(
                        member,
                        studyGroup,
                        groupApplicationForm.getNickName(),
                        groupApplicationForm.getIntro());
                return ResponseEntity.status(HttpStatus.CREATED).body("Application form is sent to admin");
            }

            return ApiError.buildApiError(
                    ErrorCode.GROUP_NICK_NAME_IS_DUPLICATED,
                    HttpStatus.UNPROCESSABLE_ENTITY);


        }


    @GetMapping("/api/groups/{groupName}/admins/members/applications")
    public ResponseEntity<Object> getUsersApplicationForm(@PathVariable String groupName){

        StudyGroup studyGroup = retrieveGroupByNameService.find(groupName);
        List<GroupApplicationList> pendingMemberList = retrieveGroupMembersApplication.getApplications(studyGroup);

        if(pendingMemberList.isEmpty()){
            return ResponseEntity.ok("No applications found");

        }
        return ResponseEntity.ok().body(pendingMemberList);

    }

    @PutMapping("/api/groups/{groupName}/admins/members/{nickName}/applications")
    public ResponseEntity<Object> ManageUserApplicationForm(@PathVariable String groupName,
                                                           @PathVariable String nickName,
                                                            @RequestParam("permission") GroupMemberApplicationConSentOrDeny permission) {
        StudyGroup studyGroup = retrieveGroupByNameService.find(groupName);
        StudyGroupMember pendingStudyGroupMember =
                retrieveGroupMemberByNickNameAndGroupService.get(studyGroup,nickName);

        handleGroupMemberApplicationService.handle(pendingStudyGroupMember,permission);

        return ResponseEntity.
                status(HttpStatus.OK).
                body("updated requested member as " + permission.name());
    }

    @DeleteMapping("/api/groups/{groupName}/members/{nickName}")
    public ResponseEntity<Object> groupUserWithdrawalUserSelfFromGroup(@PathVariable String groupName,
                                                                        @PathVariable String nickName){

        StudyGroup studyGroup = retrieveGroupByNameService.find(groupName);
        StudyGroupMember groupMember =
                retrieveGroupMemberByNickNameAndGroupService.get(studyGroup, nickName);


            if (groupMember.getGroupRole().equals(GroupRole.GROUP_ADMIN)) {
                return ApiError.
                        buildApiError(ErrorCode.TAKE_OVER_AUTHORITY_TO_GROUP_MEMBER_BEFORE_OUT,
                                HttpStatus.FORBIDDEN);
            }
            groupMemberWithdrawalMemberSelf.remove(groupMember);

            return ResponseEntity.status(HttpStatus.OK).body("Now you are out of group");

    }

    @DeleteMapping("/api/groups/{groupName}/admins/members/{nickName}")
    public ResponseEntity<Object> kickGroupUserFromGroupAsAdmin(@PathVariable String groupName,
                                                         @PathVariable String nickName){
        StudyGroup studyGroup = retrieveGroupByNameService.find(groupName);
        StudyGroupMember groupMember =
                retrieveGroupMemberByNickNameAndGroupService.get(studyGroup, nickName);

        if(groupMember.getWarnCount() < 10 ){
           return  ApiError.
                    buildApiError(
                            ErrorCode.CAN_NOT_KICK_GROUP_MEMBER_WARN_COUNT_IS_UNDER_10,
                            HttpStatus.BAD_REQUEST);
        }
        kickGroupMember.remove(groupMember);
       return  ResponseEntity.
                    status(HttpStatus.OK).
                    body("Now group member is out");

    }

    @PutMapping("/api/groups/{groupName}/admins/members/{nickName}")
    public ResponseEntity<Object> handOverAdminRole(@PathVariable String groupName,
                                                    @PathVariable String nickName) {
        //JwtUtil parse the token and set the Authentication principle as memberId
        Object memberId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        StudyGroup studyGroup = retrieveGroupByNameService.find(groupName);
        StudyGroupMember currentAdmin = retrieveGroupMemberByMemberIdAndGroup.
                get(studyGroup, ObjectToLong.convert(memberId));
        StudyGroupMember newAdmin =
                retrieveGroupMemberByNickNameAndGroupService.get(studyGroup, nickName);
        GroupRole groupMemberRole = newAdmin.getGroupRole();


        if (groupMemberRole.equals(GroupRole.GROUP_PENDING) ||
                groupMemberRole.equals(GroupRole.GROUP_DENIED)) {
            return ApiError.
                    buildApiError(
                            ErrorCode.GROUP_MEMBER_IS_NOT_QUALIFIED_BE_ADMIN,
                            HttpStatus.FORBIDDEN);
        }
        takeOverGroupAdminService.takeOver(currentAdmin, newAdmin);

        return ResponseEntity.status(HttpStatus.OK).body("hand over group admin to " + nickName);
    }

}
