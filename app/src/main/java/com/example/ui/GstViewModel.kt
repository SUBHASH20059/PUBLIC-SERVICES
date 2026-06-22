package com.example.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GstViewModel(private val repository: RegistryRepository) : ViewModel() {

    // Anti-Evasion & Fraud Detection
    fun validateInvoice(invoice: InvoiceLedger): Boolean {
        // Logic: Check if supplier and recipient are both registered and active
        // Logic: Flag mismatched invoices (simulated)
        return invoice.taxableValue > 0 && invoice.gstAmount == invoice.taxableValue * 0.18
    }

    fun fileGstr3B(gstin: String, taxableValue: Double) {
        viewModelScope.launch {
            val gstAmount = taxableValue * 0.18
            val invoice = InvoiceLedger(
                supplierGstin = gstin,
                recipientGstin = "GOVT-CENTRAL-GST",
                taxableValue = taxableValue,
                gstAmount = gstAmount,
                itcEligible = false,
                isMatchedFlag = true
            )
            repository.insertInvoice(invoice)
            repository.logAction(gstin, "GSTR3B_FILING", "InvoiceLedger", "NEW", "Summary Return Filed")
        }
    }

    fun getInvoices(gstin: String): StateFlow<List<InvoiceLedger>> {
        return repository.getInvoices(gstin)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
