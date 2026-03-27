export type GroupBalance = {
  name: string;
  initial: string;
  amount: number;
  positive: boolean;
};

export type ExpenseItem = {
  id: string;
  desc: string;
  sub: string;
  amount: string;
  share: string;
  positive: boolean;
};

export type GroupItem = {
  id: string;
  name: string;
  members: string[];
  total: number;
  balance: number;
  balances: GroupBalance[];
  expenses: ExpenseItem[];
};

export const GROUPS: GroupItem[] = [
  {
    id: "office-lunches",
    name: "Office Lunches",
    members: ["Ryan Lee", "Emily Chen", "Maria Santos", "Jake Thompson", "Leah Caones"],
    total: 5550,
    balance: 457.21,
    balances: [
      { name: "Jake Thompson", initial: "JT", amount: 732.21, positive: true },
      { name: "Emily Chen", initial: "EC", amount: 394.21, positive: true },
      { name: "Ryan Lee", initial: "RL", amount: 689.0, positive: false },
      { name: "Maria Santos", initial: "MS", amount: 320.45, positive: true }
    ],
    expenses: [
      { id: "office-pizza", desc: "You added Pizza", sub: "Office Lunches - 2026-02-27", amount: "500.00", share: "+400.00", positive: true },
      { id: "office-jollibee", desc: "Maria added Jollibee", sub: "Office Lunches - 2026-02-14", amount: "1200.00", share: "-240.00", positive: false }
    ]
  },
  {
    id: "japan-trip",
    name: "Japan Trip",
    members: ["Ryan Lee", "Emily Chen", "Maria Santos"],
    total: 2450,
    balance: -855.34,
    balances: [
      { name: "Ryan Lee", initial: "RL", amount: 320.0, positive: false },
      { name: "Emily Chen", initial: "EC", amount: 180.0, positive: false },
      { name: "Maria Santos", initial: "MS", amount: 144.66, positive: false }
    ],
    expenses: [
      { id: "japan-flight-deposit", desc: "Ryan added Flight Deposit", sub: "Japan Trip - 2026-03-01", amount: "2450.00", share: "-855.34", positive: false }
    ]
  },
  {
    id: "beach-trip",
    name: "Beach Trip",
    members: ["Ryan Lee", "Emily Chen", "Maria Santos"],
    total: 2450,
    balance: 387.45,
    balances: [
      { name: "Ryan Lee", initial: "RL", amount: 190.45, positive: true },
      { name: "Emily Chen", initial: "EC", amount: 120.0, positive: true },
      { name: "Maria Santos", initial: "MS", amount: 77.0, positive: true }
    ],
    expenses: [
      { id: "beach-cottage-fee", desc: "Emily added Cottage Fee", sub: "Beach Trip - 2026-03-08", amount: "2450.00", share: "+387.45", positive: true }
    ]
  }
];

export const ACTIVITY: ExpenseItem[] = [
  { id: "activity-pizza", desc: "You added Pizza", sub: "Office Lunches - 2026-02-27", amount: "500.00", share: "+400.00", positive: true },
  { id: "activity-internet", desc: "You added Internet Bill", sub: "Roommates - 2026-02-21", amount: "1000.00", share: "+500.00", positive: true },
  { id: "activity-settle-internet", desc: "Jake settled up Internet Bill", sub: "Roommates - 2026-02-21", amount: "1000.00", share: "+500.00", positive: true },
  { id: "activity-jollibee", desc: "Maria added Jollibee", sub: "Office Lunches - 2026-02-14", amount: "1200.00", share: "-240.00", positive: false }
];

export const SOCIAL = {
  followers: 10,
  following: 23
};

export function formatPeso(value: number) {
  return `P${Math.abs(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export function signedPeso(value: number) {
  return `${value >= 0 ? "+" : "-"}${formatPeso(value)}`;
}
