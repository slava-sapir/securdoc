package com.springprojects.securedoc.event;

import java.util.Map;
import com.springprojects.securedoc.entity.UserEntity;
import com.springprojects.securedoc.enumeration.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserEvent {
   private UserEntity user;
   private EventType type;
   private Map<?,?> data;
}
