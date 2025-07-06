package br.com.tourapp.service;


import br.com.tourapp.dto.SecurityUser;

public interface UserDetailsProvider {
    SecurityUser loadUserByUsername(String email);
}