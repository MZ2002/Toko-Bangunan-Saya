package com.example.data.repository

import com.example.data.local.ExpenseDao
import com.example.data.model.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allExpenseCategories: Flow<List<String>> = expenseDao.getAllExpenseCategories()
    val totalExpenseAmount: Flow<Double?> = expenseDao.getTotalExpensesAmount()

    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesBetweenDates(startDate, endDate)
    }

    fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double?> {
        return expenseDao.getTotalExpensesBetweenDates(startDate, endDate)
    }

    suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }
}
