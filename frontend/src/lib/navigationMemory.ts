export const LAST_CUSTOMER_PATH_KEY = "lastCustomerPath";

export function rememberCustomerPath(path: string) {
  sessionStorage.setItem(LAST_CUSTOMER_PATH_KEY, path);
}

export function getRememberedCustomerPath() {
  return sessionStorage.getItem(LAST_CUSTOMER_PATH_KEY) || "/";
}
