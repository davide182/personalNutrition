package com.nutritionists.model.entity.enums;

public enum AppointmentStatus {
    PENDING_PROPOSAL,  // In attesa che un nutrizionista accetti
    PROPOSED,          // Proposta inviata a un nutrizionista specifico
    CONFIRMED,         // Accettato e confermato
    CANCELLED,         // Cancellato dal paziente
    COMPLETED,         // Visita completata
    FAILED,            // Nessun nutrizionista ha accettato
    REJECTED_BY_NUTRITIONIST  // Rifiutato dal nutrizionista (passa al prossimo)
}
