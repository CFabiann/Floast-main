/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.roof.backend.models;


public class Tenant {
    private String name;
    private String phone;
    private String email;
    // Para Firebase, guardar la fecha como String "YYYY-MM-DD" evita problemas de conversión
    private String leaseEnd; 

    // Constructor vacío requerido por Firebase
    public Tenant() {}

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLeaseEnd() { return leaseEnd; }
    public void setLeaseEnd(String leaseEnd) { this.leaseEnd = leaseEnd; }
}