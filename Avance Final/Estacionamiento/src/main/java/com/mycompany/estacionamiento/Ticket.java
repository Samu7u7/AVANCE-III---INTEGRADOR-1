package com.mycompany.estacionamiento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    
    private String placa;
    private String nombrePersona; // *** NUEVO CAMPO ***
    private TipoServicio tipoServicio;
    private int horasContratadas;
    private int numeroEspacio;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private double montoPagado;
    private double moraPagada;
    
    public static double TARIFA_DEFINIDO = 5.0;
    public static double TARIFA_INDEFINIDO = 8.0;
    public static double MORA_POR_HORA = 3.0;
    
    // *** NUEVAS TARIFAS NOCTURNAS (12:00 AM - 6:00 AM) ***
    public static double TARIFA_DEFINIDO_NOCTURNA = 8.0;  // +3 soles
    public static double TARIFA_INDEFINIDO_NOCTURNA = 11.0; // +3 soles
    public static double MORA_POR_HORA_NOCTURNA = 6.0;    // +3 soles
    
    public enum TipoServicio {
        TIEMPO_DEFINIDO("Tiempo Definido"),
        TIEMPO_INDEFINIDO("Tiempo Indefinido");
        
        private final String descripcion;
        
        TipoServicio(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    // *** CONSTRUCTOR ORIGINAL (mantener compatibilidad) ***
    public Ticket(String placa, TipoServicio tipoServicio, int horasContratadas, int numeroEspacio) {
        this(placa, "No especificado", tipoServicio, horasContratadas, numeroEspacio);
    }
    
    // *** NUEVO CONSTRUCTOR con nombre ***
    public Ticket(String placa, String nombrePersona, TipoServicio tipoServicio, int horasContratadas, int numeroEspacio) {
        this.placa = placa;
        this.nombrePersona = nombrePersona;
        this.tipoServicio = tipoServicio;
        this.horasContratadas = horasContratadas;
        this.numeroEspacio = numeroEspacio;
        this.entrada = LocalDateTime.now();
    }
    
    // NUEVOS SETTERS para eliminar el uso de reflexión
    public void setEntrada(LocalDateTime entrada) {
        this.entrada = entrada;
    }
    
    public void setSalida(LocalDateTime salida) {
        this.salida = salida;
    }
    
    public void registrarPago(double monto) {
        this.montoPagado = monto;
    }
    
    public void registrarMora(double mora) {
        this.moraPagada = mora;
    }
    
    // *** MÉTODO CORREGIDO: Registra salida Y calcula mora automáticamente ***
    public void registrarSalida() {
        this.salida = LocalDateTime.now();
        // *** NUEVO: Calcular y registrar mora automáticamente ***
        this.moraPagada = calcularMora();
    }
    
    // *** NUEVO: Método para registrar salida con fecha específica ***
    public void registrarSalida(LocalDateTime fechaSalida) {
        this.salida = fechaSalida;
        // *** NUEVO: Calcular y registrar mora automáticamente ***
        this.moraPagada = calcularMora();
    }
    
    // *** NUEVO: Método para verificar si es horario nocturno ***
    public static boolean esHorarioNocturno(LocalDateTime fecha) {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        
        java.time.LocalTime hora = fecha.toLocalTime();
        // Horario nocturno: 00:00 (12:00 AM) hasta 06:00 (6:00 AM)
        return hora.isBefore(java.time.LocalTime.of(6, 0)) || 
               hora.equals(java.time.LocalTime.of(0, 0));
    }
    
    // *** NUEVO: Método para verificar si es horario nocturno actual ***
    public static boolean esHorarioNocturnoActual() {
        return esHorarioNocturno(LocalDateTime.now());
    }
    
    // *** MÉTODO ACTUALIZADO: Calcular total con tarifas nocturnas ***
    public double calcularTotal() {
        return calcularTotalConFecha(this.entrada);
    }
    
    // *** NUEVO: Calcular total con fecha específica ***
    public double calcularTotalConFecha(LocalDateTime fecha) {
        boolean esNocturno = esHorarioNocturno(fecha);
        
        if (tipoServicio == TipoServicio.TIEMPO_DEFINIDO) {
            double tarifa = esNocturno ? TARIFA_DEFINIDO_NOCTURNA : TARIFA_DEFINIDO;
            return horasContratadas * tarifa;
        } else {
            return esNocturno ? TARIFA_INDEFINIDO_NOCTURNA : TARIFA_INDEFINIDO;
        }
    }
    
    // *** NUEVO: Calcular total con tarifas actuales (para mostrar en tiempo real) ***
    public static double calcularTotalActual(TipoServicio tipo, int horas) {
        boolean esNocturno = esHorarioNocturnoActual();
        
        if (tipo == TipoServicio.TIEMPO_DEFINIDO) {
            double tarifa = esNocturno ? TARIFA_DEFINIDO_NOCTURNA : TARIFA_DEFINIDO;
            return horas * tarifa;
        } else {
            return esNocturno ? TARIFA_INDEFINIDO_NOCTURNA : TARIFA_INDEFINIDO;
        }
    }
    
    // *** MÉTODO MEJORADO: Calcula mora tanto para tickets finalizados como activos ***
    public double calcularMora() {
        if (tipoServicio == TipoServicio.TIEMPO_DEFINIDO) {
            LocalDateTime fechaComparacion = (salida != null) ? salida : LocalDateTime.now();
            
            long minutosUsados = java.time.Duration.between(entrada, fechaComparacion).toMinutes();
            long minutosContratados = horasContratadas * 60L;
            
            if (minutosUsados > minutosContratados) {
                long minutosExtra = minutosUsados - minutosContratados;
                long horasExtra = (minutosExtra + 59) / 60; // Redondea hacia arriba
                return horasExtra * MORA_POR_HORA;
            }
        }
        return 0.0;
    }
    
    // *** NUEVO: Método para calcular mora en tiempo real (para tickets activos) ***
    public double calcularMoraActual() {
        if (tipoServicio == TipoServicio.TIEMPO_DEFINIDO && salida == null) {
            long minutosUsados = java.time.Duration.between(entrada, LocalDateTime.now()).toMinutes();
            long minutosContratados = horasContratadas * 60L;
            
            if (minutosUsados > minutosContratados) {
                long minutosExtra = minutosUsados - minutosContratados;
                long horasExtra = (minutosExtra + 59) / 60; // Redondea hacia arriba
                return horasExtra * MORA_POR_HORA;
            }
        }
        return 0.0;
    }
    
    public long getMinutosExcedidos() {
        if (tipoServicio == TipoServicio.TIEMPO_DEFINIDO) {
            LocalDateTime fechaComparacion = (salida != null) ? salida : LocalDateTime.now();
            long minutosPermitidos = horasContratadas * 60L;
            long minutosEstacionado = java.time.Duration.between(entrada, fechaComparacion).toMinutes();
            return Math.max(0, minutosEstacionado - minutosPermitidos);
        }
        return 0;
    }
    
    // *** NUEVO: Método para obtener el total incluyendo mora actual ***
    public double getTotalConMoraActual() {
        return montoPagado + calcularMoraActual();
    }
    
    // *** ACTUALIZADO: Ticket de ingreso con nombre ***
    public String generarTicketIngreso() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticket de ingreso\n");
        sb.append("UTP Sur, Lima - Perú\n\n");
        sb.append("Placa: ").append(placa).append("\n");
        sb.append("Nombre: ").append(nombrePersona != null ? nombrePersona : "No especificado").append("\n"); // *** AGREGADO ***
        sb.append("Espacio: E-").append(numeroEspacio).append("\n");
        sb.append(formatearFecha(entrada) + "\n\n");
        sb.append("** No pierda el ticket **\n");
        sb.append("Costo por pérdida de ticket S/ 20\n");
        sb.append("|||||||||||||||||||||||||||||||||\n");
        return sb.toString();
    }
    
    // *** ACTUALIZADO: Ticket de salida con nombre ***
    public String generarTicketSalida() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticket de salida\n");
        sb.append("UTP Sur, Lima - Perú\n\n");
        sb.append("Placa: ").append(placa).append("\n");
        sb.append("Nombre: ").append(nombrePersona != null ? nombrePersona : "No especificado").append("\n"); // *** AGREGADO ***
        sb.append("Espacio: E-").append(numeroEspacio).append("\n");
        sb.append("Entrada: " + formatearFecha(entrada) + "\n");
        sb.append("Salida: " + formatearFecha(salida) + "\n");
        sb.append("Tipo: " + tipoServicio.getDescripcion() + "\n");
        
        if (tipoServicio == TipoServicio.TIEMPO_DEFINIDO) {
            sb.append("Horas contratadas: " + horasContratadas + "\n");
        }
        
        sb.append("Monto pagado: S/" + String.format("%.2f", montoPagado) + "\n");
        
        if (moraPagada > 0) {
            sb.append("Mora: S/" + String.format("%.2f", moraPagada) + "\n");
            sb.append("Tiempo excedido: " + getMinutosExcedidos() + " minutos\n");
            sb.append("Total: S/" + String.format("%.2f", montoPagado + moraPagada) + "\n");
        }
        
        sb.append("\nGracias por su visita\n");
        sb.append("|||||||||||||||||||||||||||||||||\n");
        return sb.toString();
    }
    
    /**
     * MEJORADO: Comprobante completo con todos los detalles incluyendo nombre, fecha de salida y mora
     */
    public String generarComprobante() {
        StringBuilder sb = new StringBuilder();
        sb.append("========= COMPROBANTE DE ESTACIONAMIENTO =========\n");
        sb.append("📍 UTP Sur, Lima - Perú\n");
        sb.append("📞 +51 922928818\n");
        sb.append("==================================================\n\n");
        
        sb.append("🚗 Placa: ").append(placa).append("\n");
        sb.append("👤 Nombre: ").append(nombrePersona != null ? nombrePersona : "No especificado").append("\n"); // *** AGREGADO ***
        sb.append("🅿️ Espacio: E-").append(String.format("%02d", numeroEspacio)).append("\n");
        sb.append("⏱️ Tipo de Servicio: ").append(tipoServicio.getDescripcion()).append("\n");
        sb.append("📅 Hora de Entrada: ").append(formatearFecha(entrada)).append("\n");
        
        // NUEVO: Mostrar fecha de salida si existe
        if (salida != null) {
            sb.append("📅 Hora de Salida: ").append(formatearFecha(salida)).append("\n");
            // Calcular tiempo total estacionado
            long minutosTotal = java.time.Duration.between(entrada, salida).toMinutes();
            long horas = minutosTotal / 60;
            long minutos = minutosTotal % 60;
            sb.append("⏰ Tiempo Total: ").append(horas).append("h ").append(minutos).append("m\n");
        } else {
            sb.append("📅 Hora de Salida: ⏳ Vehículo aún estacionado\n");
        }
        
        if (tipoServicio == TipoServicio.TIEMPO_DEFINIDO) {
            sb.append("🕐 Horas contratadas: ").append(horasContratadas).append("\n");
            
            // NUEVO: Mostrar tiempo restante o excedido
            if (salida != null) {
                long minutosExcedidos = getMinutosExcedidos();
                if (minutosExcedidos > 0) {
                    sb.append("⚠️ Tiempo excedido: ").append(minutosExcedidos).append(" minutos\n");
                } else {
                    long minutosUsados = java.time.Duration.between(entrada, salida).toMinutes();
                    long minutosContratados = horasContratadas * 60L;
                    long minutosRestantes = minutosContratados - minutosUsados;
                    if (minutosRestantes > 0) {
                        sb.append("✅ Tiempo restante: ").append(minutosRestantes).append(" minutos\n");
                    }
                }
            }
        }
        
        sb.append("\n💰 DETALLE DE PAGO:\n");
        sb.append("   Monto base: S/").append(String.format("%.2f", montoPagado)).append("\n");
        
        // *** MEJORADO: Mostrar mora actual o registrada ***
        double moraAMostrar = (salida != null) ? moraPagada : calcularMoraActual();
        if (moraAMostrar > 0) {
            sb.append("   ⚠️ Mora por exceso: S/").append(String.format("%.2f", moraAMostrar)).append("\n");
            sb.append("   ⏰ Tiempo excedido: ").append(getMinutosExcedidos()).append(" minutos\n");
            sb.append("   💳 TOTAL: S/").append(String.format("%.2f", montoPagado + moraAMostrar)).append("\n");
        } else {
            sb.append("   💳 TOTAL: S/").append(String.format("%.2f", montoPagado)).append("\n");
        }
        
        // NUEVO: Estado del ticket
        sb.append("\n📊 ESTADO: ");
        if (salida != null) {
            sb.append("✅ FINALIZADO\n");
        } else {
            sb.append("🔄 ACTIVO\n");
        }
        
        sb.append("\n==================================================\n");
        sb.append("⭐ ¡Gracias por su preferencia! ⭐\n");
        sb.append("🌐 www.grupointegradorutp.com\n");
        sb.append("📧 soporte@grupointegradorutp.com\n");
        sb.append("==================================================\n");
        
        return sb.toString();
    }
    
    private String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) return "N/A";
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
    
    // Getters originales
    public String getPlaca() { return placa; }
    public TipoServicio getTipoServicio() { return tipoServicio; }
    public int getHorasContratadas() { return horasContratadas; }
    public int getNumeroEspacio() { return numeroEspacio; }
    public LocalDateTime getEntrada() { return entrada; }
    public LocalDateTime getSalida() { return salida; }
    public double getMontoPagado() { return montoPagado; }
    public double getMoraPagada() { return moraPagada; }
    
    // *** NUEVOS GETTERS Y SETTERS para nombre ***
    public String getNombrePersona() {
        return nombrePersona;
    }
    
    public void setNombrePersona(String nombrePersona) {
        this.nombrePersona = nombrePersona;
    }
}