package org.example.securecoding.intelbackend.service;

import org.example.securecoding.intelbackend.domain.EmployeeSearchQuery;
import org.example.securecoding.intelbackend.domain.PhoneNumber;
import org.example.securecoding.intelbackend.domain.Role;
import org.example.securecoding.intelbackend.domain.VerifiedSession;
import org.example.securecoding.intelbackend.model.Employee;
import org.example.securecoding.intelbackend.repository.EmployeeRepository;
import org.example.securecoding.intelbackend.util.JwtValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessCardService {

    private static final String PRIVILEGED_TOKEN = "SUPER_PRIVILEGED_TOKEN_123";

    private final EmployeeRepository employeeRepository;

    public BusinessCardService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // === DEEP MODEL - AKTIF ===
    // Endpoint ini sekarang mengharuskan pemanggil untuk melakukan login terlebih
    // dahulu.
    // Hanya setelah identitas terverifikasi, token JWT yang sah diterbitkan dan
    // terikat pada userId.
    public String generateAuthenticatedToken(String userId, String role) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId tidak boleh kosong");
        }
        return JwtValidator.createToken(userId, role);

        // === KODE RENTAN (SHALLOW MODEL) ===
        // Penjelasan: Method ini membuat token dengan hak akses tinggi tanpa melakukan
        // autentikasi apapun.
        // Celah: Unauthenticated API Token Generation.
        // return PRIVILEGED_TOKEN;
    }

    // === DEEP MODEL - AKTIF ===
    // Pencarian karyawan kini hanya bisa dilakukan melalui sesi terverifikasi
    // (VerifiedSession)
    // dan query yang tidak kosong (EmployeeSearchQuery). Kedua obyek menolak nilai
    // tidak valid di konstruktornya,
    // sehingga mustahil terjadi data-dumping melalui parameter kosong.
    public List<Employee> searchEmployeesSecure(String jwt, String userId, String searchFilter) {
        VerifiedSession session = new VerifiedSession(userId, jwt);
        EmployeeSearchQuery query = new EmployeeSearchQuery(searchFilter);
        List<Employee> results = employeeRepository.findByFullNameContainingIgnoreCase(query.getValue());

        // === DEEP MODEL - ROLE-BASED MASKING ===
        // Jika bukan ADMIN, sembunyikan data sensitif seperti nomor HP dan manajer.
        if (session.getRole() != Role.ADMIN) {
            return results.stream()
                .map(emp -> new Employee(
                    emp.getId(),
                    emp.getFullName(),
                    emp.getRole(),
                    "REDACTED",
                    emp.getEmail(),
                    new PhoneNumber("+0-00-0000") // Masked phone
                ))
                .toList();
        }

        return results;
    }
}
