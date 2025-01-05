package com.templlo.service.user.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.templlo.service.user.entity.enums.Gender;
import com.templlo.service.user.entity.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, length = 50)
	private String loginId;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String userName;

	@Column(nullable = false)
	private String nickName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Gender gender;

	private String birth;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;

	@Column(length = 15)
	private String phone;

	@ColumnDefault("0")
	private int reviewCount;

	// TODO Auditor 설정 필요
	@CreatedBy
	private UUID createdBy;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedBy
	private UUID updatedBy;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	private UUID deletedBy;

	private LocalDateTime deletedAt;

	private boolean isDeleted;

	@Builder(access = AccessLevel.PRIVATE)
	public User(String phone, UserRole role, String birth, Gender gender, String nickName, String userName,
		String password, String email, String loginId) {
		this.createdAt = LocalDateTime.now();
		this.phone = phone;
		this.role = role;
		this.birth = birth;
		this.gender = gender;
		this.nickName = nickName;
		this.userName = userName;
		this.password = password;
		this.email = email;
		this.loginId = loginId;
	}

	public static User create(String loginId, String password, String email, String userName,
		String nickName, Gender gender, String birth, UserRole role, String phone) {
		return User.builder()
			.loginId(loginId)
			.password(password)
			.email(email)
			.userName(userName)
			.nickName(nickName)
			.gender(gender)
			.birth(birth)
			.role(role)
			.phone(phone)
			.build();
	}
}