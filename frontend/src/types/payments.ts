export type UnpaidSummaryDto = {
  months: string[];
  monthlyFee: string | number;
  total: string | number;
};

export type PaymentHistoryItemDto = {
  paymentId: number;
  provider: "PAYPAL" | "MANUAL";
  providerOrderId?: string | null;
  currency: string;
  amount: string | number;
  status: "PENDING" | "PAID" | "CANCELLED";
  createdAt: string;
  paidAt?: string | null;
  months: string[];
};

export type CreateOrderRequest = {
  userId: number;
  months: string[];
  currency?: string;
  returnUrl?: string;
  cancelUrl?: string;
};

export type CreateOrderResponse = {
  providerOrderId: string;
  approvalUrl: string;
  amount: string | number;
  currency: string;
  months: string[];
  status: "PENDING" | "PAID" | "CANCELLED";
};

export type CaptureResponse = {
  providerOrderId: string;
  status: string;
};
