import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";

import {
    fetchSubastas,
    fetchSubasta,
    cerrarSubasta,
    ofertarSubasta,
} from "../api/subastaApi";

describe("subastaApi", () => {
    beforeEach(() => {
        vi.stubGlobal("fetch", vi.fn());
    });

    afterEach(() => {
        vi.restoreAllMocks();
        vi.unstubAllGlobals();
    });

    describe("fetchSubastas", () => {
        test("obtiene las subastas correctamente", async () => {
            const payload = {
                subastas: [{ id: "subasta-1" }],
                page: 1,
                size: 10,
            };

            fetch.mockResolvedValue({
                ok: true,
                json: vi.fn().mockResolvedValue(payload),
            });

            const result = await fetchSubastas({
                token: "token-test",
            });

            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining("/subastas?page=1&size=10"),
                {
                    headers: {
                        Accept: "application/json",
                        Authorization: "Bearer token-test",
                    },
                }
            );

            expect(result).toEqual(payload);
        });

        test("utiliza la paginación recibida", async () => {
            fetch.mockResolvedValue({
                ok: true,
                json: vi.fn().mockResolvedValue({ subastas: [] }),
            });

            await fetchSubastas({
                page: 3,
                size: 20,
                token: "token-test",
            });

            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining("/subastas?page=3&size=20"),
                expect.any(Object)
            );
        });

        test("utiliza los valores predeterminados cuando no recibe parámetros", async () => {
            fetch.mockResolvedValue({
                ok: true,
                json: vi.fn().mockResolvedValue({ subastas: [] }),
            });

            await fetchSubastas();

            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining("/subastas?page=1&size=10"),
                expect.any(Object)
            );
        });

        test("lanza un error con el mensaje recibido en error.message", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockResolvedValue({
                    error: {
                        message: "Token inválido",
                    },
                }),
            });

            await expect(
                fetchSubastas({ token: "token-test" })
            ).rejects.toThrow("Token inválido");
        });

        test("lanza un error con el mensaje recibido en message", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockResolvedValue({
                    message: "No tenés permisos",
                }),
            });

            await expect(
                fetchSubastas({ token: "token-test" })
            ).rejects.toThrow("No tenés permisos");
        });

        test("utiliza el mensaje predeterminado cuando falla sin mensaje", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockRejectedValue(new Error("Invalid JSON")),
            });

            await expect(
                fetchSubastas({ token: "token-test" })
            ).rejects.toThrow("No se pudieron cargar las subastas.");
        });
    });

    describe("fetchSubasta", () => {
        test("obtiene una subasta correctamente", async () => {
            const payload = {
                id: "subasta-1",
                activa: true,
            };

            fetch.mockResolvedValue({
                ok: true,
                json: vi.fn().mockResolvedValue(payload),
            });

            const result = await fetchSubasta("subasta-1", "token-test");

            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining("/subastas/subasta-1"),
                {
                    headers: {
                        Accept: "application/json",
                        Authorization: "Bearer token-test",
                    },
                }
            );

            expect(result).toEqual(payload);
        });

        test("lanza un error con el mensaje recibido en error.message", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockResolvedValue({
                    error: {
                        message: "Subasta no encontrada",
                    },
                }),
            });

            await expect(
                fetchSubasta("subasta-1", "token-test")
            ).rejects.toThrow("Subasta no encontrada");
        });

        test("lanza un error con el mensaje recibido en message", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockResolvedValue({
                    message: "Acceso denegado",
                }),
            });

            await expect(
                fetchSubasta("subasta-1", "token-test")
            ).rejects.toThrow("Acceso denegado");
        });

        test("utiliza el mensaje predeterminado cuando falla sin mensaje", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockRejectedValue(new Error("Invalid JSON")),
            });

            await expect(
                fetchSubasta("subasta-1", "token-test")
            ).rejects.toThrow("No se pudo cargar la subasta.");
        });
    });

    describe("cerrarSubasta", () => {
        test("cierra una subasta correctamente", async () => {
            const payload = {
                id: "subasta-1",
                activa: false,
            };

            fetch.mockResolvedValue({
                ok: true,
                json: vi.fn().mockResolvedValue(payload),
            });

            const result = await cerrarSubasta("subasta-1", "token-test");

            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining("/subastas/subasta-1/cerrar"),
                {
                    method: "PATCH",
                    headers: {
                        Accept: "application/json",
                        Authorization: "Bearer token-test",
                    },
                }
            );

            expect(result).toEqual(payload);
        });

        test("lanza un error con el mensaje recibido en error.message", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockResolvedValue({
                    error: {
                        message: "No podés cerrar esta subasta",
                    },
                }),
            });

            await expect(
                cerrarSubasta("subasta-1", "token-test")
            ).rejects.toThrow("No podés cerrar esta subasta");
        });

        test("lanza un error con el mensaje recibido en message", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockResolvedValue({
                    message: "La subasta ya está cerrada",
                }),
            });

            await expect(
                cerrarSubasta("subasta-1", "token-test")
            ).rejects.toThrow("La subasta ya está cerrada");
        });

        test("utiliza el mensaje predeterminado cuando falla sin mensaje", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockRejectedValue(new Error("Invalid JSON")),
            });

            await expect(
                cerrarSubasta("subasta-1", "token-test")
            ).rejects.toThrow("No se pudo cerrar la subasta.");
        });
    });

    describe("ofertarSubasta", () => {
        test("realiza una oferta correctamente", async () => {
            const payload = {
                id: "oferta-1",
                puja: 15,
            };

            vi.stubGlobal("crypto", {
                randomUUID: vi.fn().mockReturnValue("uuid-test"),
            });

            fetch.mockResolvedValue({
                ok: true,
                json: vi.fn().mockResolvedValue(payload),
            });

            const result = await ofertarSubasta({
                subastaId: "subasta-1",
                puja: 15,
                token: "token-test",
            });

            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining("/subastas/ofertar"),
                {
                    method: "POST",
                    headers: {
                        Accept: "application/json",
                        "Content-Type": "application/json",
                        Authorization: "Bearer token-test",
                    },
                    body: JSON.stringify({
                        subastaId: "subasta-1",
                        puja: 15,
                        idempotencyKey: "uuid-test",
                    }),
                }
            );

            expect(result).toEqual(payload);
        });

        test("genera una nueva clave de idempotencia para cada oferta", async () => {
            const randomUUID = vi.fn()
                .mockReturnValueOnce("uuid-1")
                .mockReturnValueOnce("uuid-2");

            vi.stubGlobal("crypto", { randomUUID });

            fetch.mockResolvedValue({
                ok: true,
                json: vi.fn().mockResolvedValue({}),
            });

            const oferta = {
                subastaId: "subasta-1",
                puja: 15,
                token: "token-test",
            };

            await ofertarSubasta(oferta);
            await ofertarSubasta(oferta);

            expect(randomUUID).toHaveBeenCalledTimes(2);

            expect(JSON.parse(fetch.mock.calls[0][1].body).idempotencyKey)
                .toBe("uuid-1");

            expect(JSON.parse(fetch.mock.calls[1][1].body).idempotencyKey)
                .toBe("uuid-2");
        });

        test("lanza un error con el mensaje recibido en error.message", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockResolvedValue({
                    error: {
                        message: "La oferta es demasiado baja",
                    },
                }),
            });

            await expect(
                ofertarSubasta({
                    subastaId: "subasta-1",
                    puja: 15,
                    token: "token-test",
                })
            ).rejects.toThrow("La oferta es demasiado baja");
        });

        test("lanza un error con el mensaje recibido en message", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockResolvedValue({
                    message: "Saldo insuficiente",
                }),
            });

            await expect(
                ofertarSubasta({
                    subastaId: "subasta-1",
                    puja: 15,
                    token: "token-test",
                })
            ).rejects.toThrow("Saldo insuficiente");
        });

        test("utiliza el mensaje predeterminado cuando falla sin mensaje", async () => {
            fetch.mockResolvedValue({
                ok: false,
                json: vi.fn().mockRejectedValue(new Error("Invalid JSON")),
            });

            await expect(
                ofertarSubasta({
                    subastaId: "subasta-1",
                    puja: 15,
                    token: "token-test",
                })
            ).rejects.toThrow("No se pudo realizar la oferta.");
        });
    });
});