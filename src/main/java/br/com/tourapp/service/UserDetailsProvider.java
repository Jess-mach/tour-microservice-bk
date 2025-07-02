package br.com.tourapp.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsProvider {
    UserDetails loadUserByUsername(String email);
}