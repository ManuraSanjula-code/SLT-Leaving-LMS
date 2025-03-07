package com.slt.peotv.userservice.lms.shared.Messaging;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserMessage implements Serializable {
    private static final long serialVersionUID = 4444433441235894403L;
    private long id;
    private String userId;
    private String employeeId;
    private String email;
}

