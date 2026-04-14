# References — Directory Index

Dokumentasi referensi untuk proyek "Intel Outside" Secure Coding.

## 📂 Structure

```
references/
├── project/          # Dokumentasi proyek
├── cases/            # Analisis studi kasus per breach
├── pentest/          # Panduan penetration testing
└── planning/         # Rencana implementasi
```

---

## `project/` — Dokumentasi Proyek

| File | Deskripsi |
|---|---|
| [thisproject.md](project/thisproject.md) | Overview proyek, tech stack, prinsip keamanan |
| [architecture.md](project/architecture.md) | Package structure, API routing, class diagram |
| [dependencies.md](project/dependencies.md) | Maven dependencies dengan artifact IDs |
| [databasesetup.md](project/databasesetup.md) | H2 database setup, schema, entities, seed data |
| [api-reference.md](project/api-reference.md) | API endpoint reference (shallow + deep planned) |
| [llm-coding-constraints.md](project/llm-coding-constraints.md) | Coding rules & quality standards untuk LLM |

## `cases/` — Analisis Studi Kasus

| File | Kasus | Status |
|---|---|---|
| [IntelEmployeeDataBreach2025.md](cases/IntelEmployeeDataBreach2025.md) | Laporan breach asli | Referensi |
| [businesscardsystem.md](cases/businesscardsystem.md) | **A** — Business Card Portal | ✅ Implemented |
| [producthierarchysystem.md](cases/producthierarchysystem.md) | **B** — Product Hierarchy | ✅ Implemented |
| [seims-system.md](cases/seims-system.md) | **C** — SEIMS | ✅ Implemented |

## `pentest/` — Panduan Penetration Testing

| File | Scope |
|---|---|
| [pentest-shallow-model.md](pentest/pentest-shallow-model.md) | Overview semua kasus (A + B + C) |
| [pentest-business-card.md](pentest/pentest-business-card.md) | Kasus A — Token API + data over-fetching |
| [pentest-product-hierarchy.md](pentest/pentest-product-hierarchy.md) | Kasus B — Boolean bypass + credential exposure |
| [pentest-seims.md](pentest/pentest-seims.md) | Kasus C — Broken JWT + IDOR + NDA exposure |

## `planning/` — Rencana Implementasi

| File | Deskripsi |
|---|---|
| [deep-model-plan.md](planning/deep-model-plan.md) | Blueprint Deep Model untuk semua 3 kasus |
