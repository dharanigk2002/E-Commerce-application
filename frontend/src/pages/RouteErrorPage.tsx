import { Link, isRouteErrorResponse, useRouteError } from "react-router-dom";
import common from "../styles/common.module.css";

export function RouteErrorPage() {
  const error = useRouteError();
  const message = isRouteErrorResponse(error)
    ? error.statusText || error.data
    : error instanceof Error
      ? error.message
      : "Could not load this page";

  return (
    <section className={common.pageStack}>
      <p className={common.errorBanner}>{message}</p>
      <Link className={common.secondaryButton} to="/">
        Back to products
      </Link>
    </section>
  );
}
