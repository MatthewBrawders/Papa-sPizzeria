--New User Triggers
CREATE TRIGGER set_default_user_attributes
AFTER INSERT ON Users
FOR EACH ROW
BEGIN
    UPDATE Users
    SET role = 'customer', favoriteItem = NULL
    WHERE userID = NEW.userID;
END;

--Place Order Trigger
CREATE OR REPLACE FUNCTION auto_increment_orderID()
RETURNS TRIGGER AS $$
BEGIN
    -- Get the current maximum orderID and increment it by 1
    NEW.orderID := COALESCE((SELECT MAX(orderID) FROM FoodOrder), 10000) + 1;

    -- Return the modified NEW row
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach the trigger to the FoodOrder table
CREATE TRIGGER set_orderID
BEFORE INSERT ON FoodOrder
FOR EACH ROW
EXECUTE FUNCTION auto_increment_orderID();

--Update Order Status
CREATE TRIGGER update_order_status_timestamp
AFTER UPDATE ON FoodOrder
FOR EACH ROW
WHEN OLD.orderStatus != NEW.orderStatus
BEGIN
    UPDATE FoodOrder
    SET orderTimestamp = NOW()
    WHERE orderID = NEW.orderID;
END;

--See Order History
CREATE TRIGGER update_recent_orders
AFTER INSERT ON FoodOrder
FOR EACH ROW
BEGIN
    INSERT INTO recentOrders (userID, orderID, orderTimestamp)
    VALUES (NEW.userID, NEW.orderID, NEW.orderTimestamp);

    DELETE FROM recentOrders
    WHERE userID = NEW.userID
      AND orderTimestamp NOT IN (
          SELECT orderTimestamp
          FROM recentOrders
          WHERE userID = NEW.userID
          ORDER BY orderTimestamp DESC
          LIMIT 5
      );
END;


--If Exists Checks
    --Store
CREATE TRIGGER validate_store_on_order
BEFORE INSERT ON FoodOrder
FOR EACH ROW
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Store WHERE storeID = NEW.storeID) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid store ID.';
    END IF;
END;

    --Menu
CREATE TRIGGER prevent_unavailable_items
BEFORE INSERT ON ItemsInOrder
FOR EACH ROW
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Menu WHERE itemName = NEW.itemName AND available = 1) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Item not available.';
    END IF;
END;


