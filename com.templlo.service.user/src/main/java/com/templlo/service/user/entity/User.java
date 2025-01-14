package com.templlo.service.user.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.templlo.service.user.entity.enums.Gender;
import com.templlo.service.user.entity.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@EntityListeners(AuditingEntityListener.class)
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

	@CreatedBy
	@Column(updatable = false)
	private String createdBy;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedBy
	private String updatedBy;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	private String deletedBy;

	private LocalDateTime deletedAt;

	private boolean isDeleted;

	@Builder(access = AccessLevel.PRIVATE)
	public User(String loginId, String password, String email, String userName, String nickName, Gender gender,
		String birth, UserRole role, String phone) {
		this.loginId = loginId;
		this.password = password;
		this.email = email;
		this.userName = userName;
		this.nickName = nickName;
		this.gender = gender;
		this.birth = birth;
		this.role = role;
		this.phone = phone;
		this.isDeleted = false;
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

	public boolean increaseAndCheckReviewCount() {
		this.reviewCount++;
		return reviewCount % 5 == 0;
	}

	public void updateUser(String password, String email, String userName, String nickName,
		Gender gender, String birth, String phone) {
		this.password = password;
		this.email = email;
		this.userName = userName;
		this.nickName = nickName;
		this.gender = gender;
		this.birth = birth;
		this.phone = phone;
	}

	public void deleteUser(String loginId) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = loginId;
		this.isDeleted = true;
	}
}