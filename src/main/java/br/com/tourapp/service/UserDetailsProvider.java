package br.com.tourapp.service;


import br.com.tourapp.security.SecurityUser;

public interface UserDetailsProvider {
    SecurityUser loadUserByUsername(String email);
}