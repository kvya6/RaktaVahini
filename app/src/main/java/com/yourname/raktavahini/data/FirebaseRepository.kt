package com.yourname.raktavahini.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    val currentUserId   get() = auth.currentUser?.uid
    val currentUserEmail get() = auth.currentUser?.email
    val isLoggedIn       get() = auth.currentUser != null

    // ── Auth ──────────────────────────────────────────────
    suspend fun signUp(email: String, password: String): Result<String> = try {
        val r = auth.createUserWithEmailAndPassword(email, password).await()
        Result.success(r.user!!.uid)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signIn(email: String, password: String): Result<String> = try {
        val r = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(r.user!!.uid)
    } catch (e: Exception) { Result.failure(e) }

    fun signOut() = auth.signOut()

    // ── Donor Profile ──────────────────────────────────────
    suspend fun saveDonorProfile(donor: Donor): Result<Unit> = try {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        db.collection("donors").document(uid).set(donor.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getDonorProfile(): Result<Donor?> = try {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        val doc = db.collection("donors").document(uid).get().await()
        Result.success(if (doc.exists()) doc.toDonor() else null)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun searchDonors(bloodGroup: String, location: String): Result<List<Donor>> = try {
        var query: com.google.firebase.firestore.Query = db.collection("donors")
            .whereEqualTo("isEligible", true)
        if (bloodGroup.isNotBlank())
            query = query.whereEqualTo("bloodGroup", bloodGroup)
        val snapshot = query.get().await()
        val donors = snapshot.documents.mapNotNull { it.toDonor() }.filter { d ->
            isDonorEligible(d.lastDonationDate) &&
            (location.isBlank() || d.location.contains(location, ignoreCase = true))
        }
        Result.success(donors)
    } catch (e: Exception) { Result.failure(e) }

    // ── Donation Log ───────────────────────────────────────
    suspend fun logDonation(record: DonationRecord): Result<Unit> = try {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        db.collection("donors").document(uid).collection("donations").add(record.toMap()).await()
        db.collection("donors").document(uid).update("lastDonationDate", record.date).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getDonationLog(): Result<List<DonationRecord>> = try {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        val snap = db.collection("donors").document(uid)
            .collection("donations")
            .orderBy("date", Query.Direction.DESCENDING).get().await()
        Result.success(snap.documents.mapNotNull { it.toDonationRecord() })
    } catch (e: Exception) { Result.failure(e) }

    // ── Blood Requests ─────────────────────────────────────
    suspend fun postBloodRequest(request: BloodRequest): Result<Unit> = try {
        db.collection("bloodRequests").add(request.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getBloodRequests(): Result<List<BloodRequest>> = try {
        val snap = db.collection("bloodRequests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20).get().await()
        Result.success(snap.documents.mapNotNull { it.toBloodRequest() })
    } catch (e: Exception) { Result.failure(e) }

    // ── Stats ──────────────────────────────────────────────
    suspend fun getStats(): Result<AppStats> = try {
        val donors   = db.collection("donors").get().await().size()
        val requests = db.collection("bloodRequests").get().await().size()
        Result.success(AppStats(donors, requests))
    } catch (e: Exception) { Result.failure(Exception("Stats unavailable")) }
}

// ── Firestore <-> Model helpers ────────────────────────────
fun Donor.toMap() = mapOf(
    "name" to name, "bloodGroup" to bloodGroup, "location" to location,
    "phone" to phone, "lastDonationDate" to lastDonationDate, "isEligible" to isEligible
)

fun com.google.firebase.firestore.DocumentSnapshot.toDonor() = try {
    Donor(
        id = id,
        name = getString("name") ?: "",
        bloodGroup = getString("bloodGroup") ?: "",
        location = getString("location") ?: "",
        phone = getString("phone") ?: "",
        lastDonationDate = getString("lastDonationDate") ?: "",
        isEligible = getBoolean("isEligible") ?: true
    )
} catch (e: Exception) { null }

fun DonationRecord.toMap() = mapOf("date" to date, "location" to location, "note" to note)

fun com.google.firebase.firestore.DocumentSnapshot.toDonationRecord() = try {
    DonationRecord(
        id = id,
        date = getString("date") ?: "",
        location = getString("location") ?: "",
        note = getString("note") ?: ""
    )
} catch (e: Exception) { null }

fun BloodRequest.toMap() = mapOf(
    "bloodGroup" to bloodGroup, "hospital" to hospital, "city" to city,
    "contactName" to contactName, "contactPhone" to contactPhone,
    "urgency" to urgency, "timestamp" to timestamp, "postedByUid" to postedByUid
)

fun com.google.firebase.firestore.DocumentSnapshot.toBloodRequest() = try {
    BloodRequest(
        id = id,
        bloodGroup = getString("bloodGroup") ?: "",
        hospital   = getString("hospital") ?: "",
        city       = getString("city") ?: "",
        contactName  = getString("contactName") ?: "",
        contactPhone = getString("contactPhone") ?: "",
        urgency    = getString("urgency") ?: "Normal",
        timestamp  = getString("timestamp") ?: "",
        postedByUid = getString("postedByUid") ?: ""
    )
} catch (e: Exception) { null }