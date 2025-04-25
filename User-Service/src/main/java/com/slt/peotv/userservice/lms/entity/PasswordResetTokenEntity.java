package com.slt.peotv.userservice.lms.entity;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "password_reset_tokens")
@Data
@EqualsAndHashCode
public class PasswordResetTokenEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8051324316462829780L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String token;

	@OneToOne()
	@JoinColumn(name = "users_id")
	private UserEntity userDetails;
}
