package com.example.lifolio.service;

import com.example.lifolio.converter.UserConverter;
import com.example.lifolio.dto.user.UserRes;
import com.example.lifolio.entity.Alarm;
import com.example.lifolio.entity.User;
import com.example.lifolio.repository.AlarmRepository;
import com.example.lifolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    public UserRes.AlarmList getAlarmList(Long userId) {
        User user = userRepository.getOne(userId);

        Alarm alarm = alarmRepository.findByUserId(userId);

        int allAlarm=1;
        int myAlarm=1;
        int planningAlarm=1;
        int socialAlarm=1;

        //MyAlarm 설정
        if(alarm.getWeekAlarm()==0&&alarm.getBadgeAlarm()==0){
            myAlarm=0;
        }
        //PlanningAlarm
        if(alarm.getTodoAlarm()==0&&alarm.getGoalAlarm()==0){
            planningAlarm=0;
        }
        //SocialAlarm
        if(alarm.getUploadAlarm()==0&&alarm.getInterestAlarm()==0){
            socialAlarm=0;
        }
        if(socialAlarm==0&&myAlarm==0&&planningAlarm==0){
            allAlarm=0;
        }

        return UserConverter.GetAlarmList(alarm,allAlarm,socialAlarm,myAlarm,planningAlarm);

    }
}
