# 🛒 Shopping List App

A modern, multi-list Android shopping application built with **Kotlin Multiplatform (Room 3.0)**, **Jetpack Compose**, and **MVVM architecture**. Designed to streamline grocery trips, manage history, and track completion states dynamically.

---

## 🚀 Features

* **Multi-List Management:** Create, view, and delete multiple shopping lists effortlessly.
* **Dynamic Status Tracking:** Automatically evaluates list status. Displays **"Pendente"** (Pending) if there are unpurchased items, or **"Finalizada ✓"** (Completed) in green when all items are checked off.
* **Detailed Timestamps:** Automatically records and displays the precise creation date and time (`dd/MM/yyyy às HH:mm`) for every list.
* **Item Management:** Add items, adjust quantities, mark items as purchased (with state-driven green buttons), and delete unwanted items.
* **Finish Purchase Flow:** Finalize your shopping session with a single tap, updating the list status instantly.

---

## 🛠️ Tech Stack & Architecture

* **UI:** Jetpack Compose (Declarative UI)
* **Architecture:** MVVM (Model-View-ViewModel) + StateFlow
* **Local Database:** Room 3.0 (KMP) with Coroutines support
* **Language:** Kotlin

---

## 📱 Screenshots / UI Overview

*(Add screenshots of your app here if desired)*

---

## 📦 Getting Started

1. Clone the repository:
   ```bash
   git clone [https://github.com/your-username/your-repo-name.git](https://github.com/your-username/your-repo-name.git)
