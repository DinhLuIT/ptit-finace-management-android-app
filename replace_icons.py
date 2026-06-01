import os

layouts = [
    "app/src/main/res/layout/item_transaction.xml",
    "app/src/main/res/layout/item_recurring_transaction.xml",
    "app/src/main/res/layout/item_category.xml",
    "app/src/main/res/layout/item_category_expense.xml"
]

for layout in layouts:
    if not os.path.exists(layout): continue
    with open(layout, "r") as f:
        content = f.read()
    content = content.replace("<TextView\n            android:id=\"@+id/tv_category_icon\"", "<ImageView\n            android:id=\"@+id/tv_category_icon\"")
    # For item_transaction.xml
    content = content.replace("android:textColor=\"@color/white\"\n            android:textSize=\"18sp\" />", "android:padding=\"8dp\"\n            app:tint=\"@color/white\" />")
    # For others
    content = content.replace("android:textSize=\"22sp\"\n            android:background=", "android:padding=\"10dp\"\n            app:tint=\"@color/white\"\n            android:background=")
    content = content.replace("android:textSize=\"20sp\"\n            android:background=", "android:padding=\"10dp\"\n            app:tint=\"@color/white\"\n            android:background=")
    content = content.replace("</TextView>", "</ImageView>")
    with open(layout, "w") as f:
        f.write(content)

adapters = [
    "app/src/main/java/com/ptithcm/finacemanager/adapter/TransactionAdapter.java",
    "app/src/main/java/com/ptithcm/finacemanager/adapter/RecurringTransactionAdapter.java",
    "app/src/main/java/com/ptithcm/finacemanager/adapter/CategoryAdapter.java",
    "app/src/main/java/com/ptithcm/finacemanager/adapter/CategoryExpenseAdapter.java"
]

for adapter in adapters:
    if not os.path.exists(adapter): continue
    with open(adapter, "r") as f:
        content = f.read()
    
    if "import android.widget.ImageView;" not in content:
        content = content.replace("import android.widget.TextView;", "import android.widget.TextView;\nimport android.widget.ImageView;")
    
    content = content.replace("TextView tvCategoryIcon;", "ImageView tvCategoryIcon;")
    content = content.replace("TextView textViewCategoryIcon;", "ImageView textViewCategoryIcon;")
    content = content.replace("(TextView) itemView.findViewById(R.id.tv_category_icon)", "(ImageView) itemView.findViewById(R.id.tv_category_icon)")
    
    # Replace setText(IconMapper.toEmoji(...))
    if "TransactionAdapter" in adapter:
        content = content.replace("textViewCategoryIcon.setText(IconMapper.toEmoji(transaction.getCategoryIcon()));", 
                                  "textViewCategoryIcon.setImageResource(IconMapper.getIconResource(context, transaction.getCategoryIcon()));")
    elif "RecurringTransactionAdapter" in adapter:
        content = content.replace("holder.tvCategoryIcon.setText(IconMapper.toEmoji(recurring.getCategoryIcon()));",
                                  "holder.tvCategoryIcon.setImageResource(IconMapper.getIconResource(context, recurring.getCategoryIcon()));")
    elif "CategoryAdapter" in adapter:
        content = content.replace("holder.tvCategoryIcon.setText(IconMapper.toEmoji(category.getIcon()));",
                                  "holder.tvCategoryIcon.setImageResource(IconMapper.getIconResource(context, category.getIcon()));")
    elif "CategoryExpenseAdapter" in adapter:
        content = content.replace("holder.tvCategoryIcon.setText(IconMapper.toEmoji(categoryExpense.getCategoryIcon()));",
                                  "holder.tvCategoryIcon.setImageResource(IconMapper.getIconResource(context, categoryExpense.getCategoryIcon()));")
    with open(adapter, "w") as f:
        f.write(content)

print("Replaced TextView with ImageView and IconMapper calls.")
