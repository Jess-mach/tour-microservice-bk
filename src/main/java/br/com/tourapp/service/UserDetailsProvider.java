package br.com.tourapp.service;


import br.com.tourapp.entity.SecurityUser;

public interface UserDetailsProvider {
    SecurityUser loadUserByUsername(String email);
}