-- ============================================================
-- MIGRACIÓN: Normalizar valores legacy de venta.estado
-- Ejecutar una sola vez en MySQL Workbench contra la BD
--   ecommerceallprocess
-- ============================================================

USE ecommerceallprocess;

-- Ver cuántas filas tienen valores legacy ANTES de migrar
SELECT estado, COUNT(*) AS cantidad
FROM venta
WHERE estado IN ('PROCESANDO', 'EN_CAMINO', 'COMPLETADO')
GROUP BY estado;

-- Migrar: PROCESANDO → EN_PROCESO
UPDATE venta SET estado = 'EN_PROCESO'  WHERE estado = 'PROCESANDO';

-- Migrar: EN_CAMINO → ENVIADA
UPDATE venta SET estado = 'ENVIADA'     WHERE estado = 'EN_CAMINO';

-- Migrar: COMPLETADO → ENTREGADA
UPDATE venta SET estado = 'ENTREGADA'   WHERE estado = 'COMPLETADO';

-- Verificar que no queden valores inválidos
SELECT estado, COUNT(*) AS cantidad
FROM venta
GROUP BY estado
ORDER BY estado;

-- Resultado esperado: solo valores del enum actual
-- PENDIENTE, PENDIENTE_PAGO, PAGADA, PAGO_RECHAZADO, PAGO_CANCELADO,
-- EN_PROCESO, ENVIADA, ENTREGADA, CANCELADA
