package com.donte.budget.api.security.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeradorSenha {

	public static void main(String[] args) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(encoder.encode("Abc123"));
		
		System.out.println(encoder.matches("Abc123", "$2a$10$rKvZjZTt4n9jMhfbkKwB4.dUjD9z84ZW0U92P8BIRAlp7a.VQdoFW"));
		System.out.println(encoder.matches("Abc123", "$2a$10$CXt10CVBgQOvU8JIKBoxqeMBoJp8XvRBrhJRxc36MD25g7EOWQXMa"));
		
	//	apasswordEncoder.matches(lSenhaAntiga, aUsuario.getPassword())
	}
	
}
