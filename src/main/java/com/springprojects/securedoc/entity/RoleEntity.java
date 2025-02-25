package com.springprojects.securedoc.entity;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.springprojects.securedoc.enumeration.Authority;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
@JsonInclude(NON_DEFAULT)
public class RoleEntity extends Auditable {
   private String name;
   private Authority authorities; 
}
