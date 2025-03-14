package com.slt.peotv.userservice.lms.shared.Messaging;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMessage implements Serializable {
    private static final long serialVersionUID = 4444433441235894403L;
    private long id;
    private String userId;
    private String employeeId;
    private String email;
}

