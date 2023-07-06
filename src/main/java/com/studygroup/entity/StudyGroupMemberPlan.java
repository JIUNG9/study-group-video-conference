package com.studygroup.entity;

import javax.persistence.*;

import lombok.*;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
@Table(name ="study_group_member_plan")
public class StudyGroupMemberPlan extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private java.util.Date startDate;

    @Temporal(TemporalType.DATE)
    private java.util.Date endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean is_achieved;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private StudyGroupMember studyGroupMember;
}