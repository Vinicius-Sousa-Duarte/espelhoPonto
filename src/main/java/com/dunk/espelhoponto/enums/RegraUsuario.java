package com.dunk.espelhoponto.enums;

public enum RegraUsuario {

    ADMIN("admin"),
    USER("user");

    private String role;

    RegraUsuario(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }
}