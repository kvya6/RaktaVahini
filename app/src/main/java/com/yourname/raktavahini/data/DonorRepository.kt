package com.yourname.raktavahini.data

object DonorRepository {

    private val donors = mutableListOf(
        Donor("1","Ananya Sharma","A+","Bengaluru","9876543210","2024-10-01"),
        Donor("2","Rahul Verma","B+","Bengaluru","9123456780","2025-12-01"),
        Donor("3","Priya Nair","O+","Mysuru","9988776655","2025-01-15"),
        Donor("4","Kiran Rao","AB+","Bengaluru","9001122334",""),
        Donor("5","Deepa Pillai","A-","Bengaluru","9876501234","2026-03-10"),
    )

    val donationLog = mutableListOf<DonationRecord>()

    fun getAllDonors(): List<Donor> = donors.toList()

    fun searchDonors(bloodGroup: String, location: String): List<Donor> {
        return donors.filter { d ->
            (bloodGroup.isBlank() || d.bloodGroup == bloodGroup) &&
            (location.isBlank() || d.location.contains(location, ignoreCase = true)) &&
            isDonorEligible(d.lastDonationDate) &&
            d.isEligible
        }
    }

    fun registerOrUpdateDonor(donor: Donor) {
        val idx = donors.indexOfFirst { it.id == donor.id }
        if (idx >= 0) donors[idx] = donor else donors.add(donor)
    }

    fun logDonation(record: DonationRecord) {
        donationLog.add(0, record)
    }
}
