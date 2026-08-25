# AlHajj Yasser Markets AI 📈💰

**منصة تحليل ذكية للأسواق المالية المصرية والذهب**

## 🌟 المميزات الرئيسية

- 📊 تحليل فني وأساسي للأسواق المالية المصرية
- 🥇 أسعار الذهب الحية (الجرام والكيلو)
- 📱 تطبيق موبايل أصلي (iOS و Android بتطبيق موحد)
- 🖥️ تطبيق ويب متقدم (React.js)
- 🖥️ تطبيق ديسكتوب (Windows و macOS)
- 🔔 تنبيهات فورية عند تغير الأسعار
- 💼 إدارة المحفظة الاستثمارية
- 🔐 نظام أمان عالي والتشفير
- 📈 رسوم بيانية متقدمة وحية
- 🌙 دعم الوضع الليلي
- 🌐 دعم اللغة العربية بالكامل

## 📁 بنية المشروع

```
AlHajj-Yasser-Markets-AI/
├── backend/                    # Backend API (Node.js + Express)
│   ├── src/
│   │   ├── routes/            # المسارات
│   │   ├── models/            # نماذج قاعدة البيانات
│   │   ├── controllers/       # المتحكمات
│   │   └── index.js           # نقطة البداية
│   ├── package.json
│   ├── Dockerfile
│   └── .env.example
├── web/                        # Frontend Web (React.js)
│   ├── src/
│   │   ├── components/        # المكونات
│   │   ├── pages/             # الصفحات
│   │   ├── App.js
│   │   └── index.js
│   └── package.json
├── mobile/                     # Mobile App (React Native)
│   ├── screens/               # الشاشات
│   ├── App.js
│   └── package.json
├── desktop/                    # Desktop App (Electron)
│   ├── public/
│   │   └── electron.js
│   └── package.json
├── docs/                       # التوثيق
├── .github/
│   └── workflows/             # CI/CD Workflows
├── docker-compose.yml
├── package.json
└── .gitignore
```

## 🚀 البدء السريع

### المتطلبات
- Node.js 18+
- npm أو yarn
- Git

### التثبيت والتشغيل

```bash
# 1. استنساخ المشروع
git clone https://github.com/yasser410/AlHajj-Yasser-Markets-AI.git
cd AlHajj-Yasser-Markets-AI

# 2. تثبيت جميع المشاريع
npm run install:all

# 3. بدء التطوير (جميع الخدمات)
npm run dev:all
```

### بدء الخدمات بشكل منفصل

**Backend API:**
```bash
cd backend
npm install
npm run dev
# متاح على: http://localhost:5000
```

**Web Application:**
```bash
cd web
npm install
npm start
# متاح على: http://localhost:3000
```

**Mobile App:**
```bash
cd mobile
npm install
npm run android    # للأندرويد
npm run ios        # لـ iOS
```

**Desktop App:**
```bash
cd desktop
npm install
npm start
```

### باستخدام Docker

```bash
docker-compose up
```

## 📊 API Endpoints

### الصحة والحالة
```
GET /api/health
```

### الأسواق
```
GET /api/markets/egx30      # مؤشر EGX30
GET /api/markets/stocks     # قائمة الأسهم
GET /api/markets/gold       # سعر الذهب
```

### الأسعار
```
GET /api/prices/live        # الأسعار الحية
GET /api/prices/history     # سجل الأسعار
```

### المحفظة
```
GET /api/portfolio           # الحصول على المحفظة
POST /api/portfolio/add      # إضافة استثمار
```

### التحليل
```
GET /api/analysis/technical   # التحليل الفني
GET /api/analysis/fundamental # التحليل الأساسي
```

## 🔧 البيئات المدعومة

- ✅ **Web**: Chrome, Firefox, Safari, Edge
- ✅ **Mobile**: iOS 13+, Android 8+
- ✅ **Desktop**: Windows 10+, macOS 10.12+
- ✅ **Tablet**: iPad, Android Tablets
- ✅ **PWA**: Progressive Web App

## 📚 التوثيق

- [دليل التثبيت](./docs/INSTALLATION.md)
- [مستندات API](./docs/API.md)
- [دليل المساهمة](./docs/CONTRIBUTING.md)

## 🛠️ التقنيات المستخدمة

### Backend
- Node.js + Express
- MongoDB
- Redis
- JWT Authentication
- Socket.io (للبيانات الحية)

### Frontend Web
- React.js 18
- React Router
- Axios
- Recharts (الرسوم البيانية)
- Ant Design (المكونات)

### Mobile
- React Native
- React Navigation
- Axios
- AsyncStorage

### Desktop
- Electron
- React.js
- Electron Builder

## 👨‍💻 المطور

**الحاج ياسر** - تطوير المشروع

## 📄 الترخيص

MIT License - يمكنك استخدام هذا المشروع بحرية

## 📞 التواصل والدعم

- 📧 البريد الإلكتروني: yasser@example.com
- 🐙 GitHub: github.com/yasser410
- 💬 Issues: للإبلاغ عن المشاكل والاقتراحات

## 🤝 المساهمة

نرحب بمساهماتك! يرجى قراءة [دليل المساهمة](./docs/CONTRIBUTING.md)

---

**آخر تحديث**: 25 أغسطس 2026

🌟 إذا أعجبك المشروع، لا تنسى وضع نجمة ⭐
