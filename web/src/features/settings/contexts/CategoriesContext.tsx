import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import type { CategoriesContextType } from "../types/settings";

const CategoriesContext = createContext<CategoriesContextType | undefined>(undefined);

const PREDEFINED_CATEGORIES = ["Food", "Transport", "Entertainment", "Utilities", "Shopping", "Healthcare", "Education", "Other"] as const;

export function CategoriesProvider({ children }: { children: ReactNode }) {
  const [selectedCategories, setSelectedCategories] = useState<string[]>(Array.from(PREDEFINED_CATEGORIES));

  // Load from localStorage on mount
  useEffect(() => {
    const saved = localStorage.getItem("selectedCategories");
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed)) {
          setSelectedCategories(parsed);
        }
      } catch (e) {
        console.error("Failed to parse categories from localStorage", e);
      }
    }
  }, []);

  // Save to localStorage whenever selectedCategories changes
  useEffect(() => {
    localStorage.setItem("selectedCategories", JSON.stringify(selectedCategories));
  }, [selectedCategories]);

  return (
    <CategoriesContext.Provider value={{ selectedCategories, setSelectedCategories }}>
      {children}
    </CategoriesContext.Provider>
  );
}

export function useCategories() {
  const context = useContext(CategoriesContext);
  if (!context) {
    throw new Error("useCategories must be used within CategoriesProvider");
  }
  return context;
}

export { PREDEFINED_CATEGORIES };
