package com.studygroup.service.group;

import com.studygroup.entity.StudyGroup;
import org.springframework.stereotype.Service;

@Service
public interface GroupUpdateInfoService {
    void update(StudyGroup group, String updateInfo);
}