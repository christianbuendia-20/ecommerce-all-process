-- ============================================================
-- MIGRACIÓN: Normalizar valores legacy de pago.metodo_pago
-- Ejecutar una sola vez en MySQL Workbench contra la BD
--   ecommerceallprocess
-- ============================================================

USE ecommerceallprocess;

-- Ver cuántas filas tienen valores legacy ANTES de migrar
SELECT metodo_pago, COUNT(*) AS cantidad
FROM pago
WHERE metodo_pago IN ('TARJETA_CREDITO', 'TARJETA_DEBITO', 'TRANSFERENCIA_BANCARIA', 'PAYPAL')
GROUP BY metodo_pago;

-- Migrar: TARJETA_CREDITO → TARJETA
UPDATE pago SET metodo_pago = 'TARJETA'       WHERE metodo_pago = 'TARJETA_CREDITO';

-- Migrar: TARJETA_DEBITO → TARJETA
UPDATE pago SET metodo_pago = 'TARJETA'       WHERE metodo_pago = 'TARJETA_DEBITO';

-- Migrar: TRANSFERENCIA_BANCARIA → TRANSFERENCIA
UPDATE pago SET metodo_pago = 'TRANSFERENCIA' WHERE metodo_pago = 'TRANSFERENCIA_BANCARIA';

-- Migrar: PAYPAL → TRANSFERENCIA
UPDATE pago SET metodo_pago = 'TRANSFERENCIA' WHERE metodo_pago = 'PAYPAL';

-- Verificar que no queden valores inválidos
SELECT metodo_pago, COUNT(*) AS cantidad
FROM pago
GROUP BY metodo_pago
ORDER BY metodo_pago;

-- Resultado esperado: solo valores del enum actual
-- TARJETA, YAPE, TRANSFERENCIA, MERCADO_PAGO
