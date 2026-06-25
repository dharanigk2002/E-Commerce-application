import type { AddressPayload, User } from "../types";

export function addressFromUser(user: User): AddressPayload {
  return {
    shippingAddressLine: user.shippingAddressLine || "",
    shippingCity: user.shippingCity || "",
    shippingState: user.shippingState || "",
    shippingPostalCode: user.shippingPostalCode || "",
    shippingCountry: user.shippingCountry || "",
  };
}

export function hasAddress(user: User) {
  return Boolean(
    user.shippingAddressLine &&
      user.shippingCity &&
      user.shippingState &&
      user.shippingPostalCode &&
      user.shippingCountry,
  );
}

export function formatAddress(user: User) {
  return [
    user.shippingAddressLine,
    user.shippingCity,
    user.shippingState,
    user.shippingPostalCode,
    user.shippingCountry,
  ]
    .filter(Boolean)
    .join(", ");
}
