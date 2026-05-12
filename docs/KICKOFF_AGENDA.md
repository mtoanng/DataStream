# 🤝 Kickoff Meeting Agenda

> **Thời lượng**: 45 phút
> **Người tham gia**: Leader + Android Developer
> **Mục tiêu**: Hai bên thống nhất scope, stack, deadline, phương thức làm việc trước khi Android Dev bắt đầu code.

---

## 📅 Chuẩn bị trước meeting

### Android Developer làm trước

- [ ] Đã đọc `docs/ANDROID_ONBOARDING.md` (full ~15 phút)
- [ ] Đã đọc `docs/API_CONTRACT.md` (skim, focus mục Auth + 1-2 endpoint)
- [ ] Đã đọc `docs/ARCHITECTURE.md` (skim cấu trúc folder)
- [ ] Đã cài Android Studio + tạo emulator Pixel 5 API 34
- [ ] Đã tạo project sandbox `HelloAndroid` test "Hello World" chạy được
- [ ] Đã chạy mock server (`mock/start.sh` hoặc `start.ps1`) — confirm 14 endpoint trả 200
- [ ] Chuẩn bị **1 mockup nhanh** (Figma / draw.io / paper sketch) cho 2-3 màn chính

### Leader làm trước

- [ ] Sẵn sàng demo Swagger UI (mock URL hoặc backend live)
- [ ] Generate sẵn 1 JWT token mẫu (8 giờ) gửi Android Dev qua chat
- [ ] Setup Postman workspace + invite Android Dev (nếu dùng Postman Mock)
- [ ] Sẵn sàng share screen
- [ ] Có danh sách stretch goal optional (Map / LLM Insight / FCM) để bàn

---

## 📋 Agenda (45 phút)

### Phần 1: Introductions + workflow (5')

- Giới thiệu nhau, kinh nghiệm Android trước đó
- Workflow: daily standup 5' trong group chat, weekly review 30' qua call
- Tool chat: Zalo / Discord / Telegram (Leader chọn)

---

### Phần 2: Demo backend hiện tại (10')

**Leader thực hiện**:
1. Share Swagger UI (`http://localhost:8090/swagger-ui.html`) hoặc mock URL
2. Demo từng endpoint group:
   - Auth: login với admin/admin → trả JWT
   - Security: score + cascade-risks
   - 4 Pillars: outlook (Pillar 1), volatility (Pillar 2)
   - Recommendations: list + acknowledge
   - Alerts: active
3. Highlight: shape data, enum values, JSON field naming convention

**Android Dev hỏi**:
- Field nào nullable?
- Date format chuẩn (ISO 8601 UTC vs local)?
- Có pagination không (hiện không có, nhưng có thể thêm)?

---

### Phần 3: Walk through API contract qua Postman (10')

**Leader**:
1. Mở Postman collection (Leader share trước trong chat)
2. Chạy 14 request tuần tự → confirm cùng response với Swagger
3. Show cách lấy token từ login → auto-fill vào header authorization

**Android Dev**:
- Confirm các DTO trong `docs/API_CONTRACT.md` match Postman response
- Note xuống endpoint nào "tricky" (nested JSON, nullable field) để xử lý cẩn thận

---

### Phần 4: Android Dev trình bày stack đã chọn + mockup (10')

**Android Dev thực hiện**:
1. Show `docs/ARCHITECTURE.md` mục Tech stack — confirm chọn:
   - [ ] Kotlin hay Java?
   - [ ] XML hay Compose?
   - [ ] MVVM hay alternative?
   - [ ] Material 3 hay Material 2?
2. Show mockup 2-3 màn:
   - Login screen
   - Home dashboard (4 KPI + Security Score)
   - Pillar 3 tab (chart + table)
3. Giải thích flow nav: Splash → Login → Main (bottom-nav) → Detail

**Leader phản hồi**:
- OK hết → proceed
- Hoặc đề xuất tinh chỉnh (vd "anh thấy Security Score nên hiện ở góc trên-phải Home thay vì giữa")

---

### Phần 5: Thống nhất 5-7 PR + deadline (5')

Mở `docs/ANDROID_ONBOARDING.md §10` — confirm 7 PR roadmap:

| PR | Mô tả | Time |
|----|-------|------|
| 1 | Bootstrap | 3h |
| 2 | Splash + Login | 4h |
| 3 | Main shell + Home | 5h |
| 4 | 4 Pillar tabs | 7h |
| 5 | Alerts + Recommendations | 5h |
| 6 | Settings + About + i18n | 3h |
| 7 | Polish + APK + demo | 3h |

**Thống nhất**:
- Deadline cứng môn Android: __________ (Leader cập nhật)
- Buffer 1 tuần cuối cho debug + dress rehearsal
- Mỗi PR review trong 24h
- Daily standup: __________ (giờ cố định, vd 9:00 PM)

---

### Phần 6: Demo cách deploy + test trên điện thoại thật (5')

**Leader giải thích**:
1. Backend chạy trên máy Leader (port 8090)
2. Trong khi WiFi cùng mạng → Android Dev đổi BASE_URL = `http://<IP-Leader>:8090`
3. Nếu khác mạng → Leader bật `cloudflared tunnel` → trả URL `https://*.trycloudflare.com`
4. Android Dev cập nhật BASE_URL qua màn Settings → app gọi tunnel URL

**Android Dev confirm**:
- Có điện thoại Android cá nhân không?
- Phiên bản Android (cần ≥ 7.0)?
- Có dây cáp USB để cài APK trực tiếp không?

---

## 🎯 Đầu ra meeting

### Tài liệu

- [ ] Stack quyết định: Kotlin/Java + XML/Compose + Material 3 → ghi vào commit message PR1
- [ ] Roadmap 7 PR + deadline cụ thể từng PR → tạo GitHub Project Board
- [ ] BASE_URL strategy (mock → live → tunnel) → document trong README repo Android

### Access

- [ ] Android Dev có GitHub access vào repo DataStream (write) — Leader add collaborator
- [ ] JWT token mẫu 8h trong chat
- [ ] Postman collection share
- [ ] Group chat invite

### Action items

- [ ] Android Dev: Hôm sau bắt đầu PR1 (Bootstrap)
- [ ] Leader: Sẵn sàng review PR trong 24h
- [ ] Cả hai: Schedule daily standup vào __________

---

## ❓ Câu hỏi mẫu Android Dev nên hỏi

### Technical

- [ ] Khi nào backend Phase 4.5 build xong (chuyển từ mock sang live)?
- [ ] Có cần support tablet không hay chỉ phone?
- [ ] Min Android version cứng là gì? (Khuyến nghị API 24 — Android 7.0)
- [ ] Có target API level cao (34) hay vẫn dùng 33 để tránh permission mới?
- [ ] Có yêu cầu offline mode không? (Khuyến nghị: không — saves Room work)
- [ ] App có cần dark mode bắt buộc không?
- [ ] Phải làm màn Map không? Nếu có, OSM (free) hay Google Maps (cần API key)?
- [ ] Có cần LLM Insight (Gemini) không? Nếu có, ai cấp API key?

### Project management

- [ ] Báo cáo môn Android theo template nào? (Leader có template không?)
- [ ] Có dress rehearsal trước bảo vệ không?
- [ ] Demo trên emulator của máy demo hay điện thoại Dev?
- [ ] Có backup plan nếu backend down lúc demo? (Có — mock server)
- [ ] Slide có cần làm không hay chỉ video demo?

### Process

- [ ] Code review: Leader review chi tiết hay chỉ pass-through?
- [ ] Có CI/CD setup không (GitHub Actions build APK)? Hoặc cần Dev tự build?
- [ ] Khi Leader bận, ai backup review PR?

---

## 🚦 Sau meeting

### Tuần 1 (Android Dev)

- Hôm sau meeting: bắt đầu PR1 (Bootstrap)
- Cuối tuần 1: hoàn thành PR1 + PR2 (Login)

### Tuần 1 (Leader)

- Setup branch protection trên repo DataStream
- Add Android Dev as collaborator
- Maintain mock server (update db.json khi backend logic đổi)

---

## 📝 Template ghi chú meeting

```markdown
# Kickoff Meeting Notes — VES Monitor Mobile

**Ngày**: _________ (2026-05-__)
**Tham gia**: Leader (_____) + Android Dev (_____)
**Thời lượng thực tế**: ___ phút

## Quyết định

### Stack
- Language: Kotlin / Java
- UI: XML + Material 3 / Compose
- Architecture: MVVM
- Min SDK: 24, Target: 34

### Roadmap
| PR | Deadline |
|----|----------|
| 1. Bootstrap | __________ |
| 2. Login | __________ |
| ... | ... |
| 7. Polish | __________ |

### Demo plan
- Backend host: máy Leader / Cloudflared tunnel
- Demo device: emulator / điện thoại Dev / cả hai

## Action items

- [ ] Leader: ___________________________________________ (deadline: ____)
- [ ] Android Dev: _______________________________________ (deadline: ____)
- [ ] Cả hai: ____________________________________________ (deadline: ____)

## Open questions
- ___________________________________________________________
- ___________________________________________________________

## Next sync
- Daily standup: _______ (qua _______)
- Weekly review: _______ (qua _______)
```

---

> 💡 **Lưu file ghi chú vào**: `docs/meeting-notes/kickoff_YYYY-MM-DD.md` trong repo này.
> Mỗi meeting sau (PR1 review, mid-project, dress rehearsal) đều save lại để Chương 4 báo cáo (Quản lý dự án) có evidence.
