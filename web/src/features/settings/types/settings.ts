export type Theme = "light" | "dark" | "system";

export interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  isDark: boolean;
}

export interface CategoriesContextType {
  selectedCategories: string[];
  setSelectedCategories: (categories: string[]) => void;
}
