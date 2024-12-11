--New User Triggers
CREATE OR REPLACE FUNCTION set_default_user_attributes()
RETURNS TRIGGER AS $$
BEGIN
    -- Set default role and favoriteItem
    NEW.role := 'customer';
    NEW.favoriteItem := NULL;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_default_user_attributes_trigger
BEFORE INSERT ON Users
FOR EACH ROW
EXECUTE FUNCTION set_default_user_attributes();

--Place Order Trigger
CREATE OR REPLACE FUNCTION auto_increment_orderID()
RETURNS TRIGGER AS $$
BEGIN
    -- Generate the next orderID
    NEW.orderID := COALESCE((SELECT MAX(orderID) FROM FoodOrder), 10000) + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_orderID_trigger
BEFORE INSERT ON FoodOrder
FOR EACH ROW
EXECUTE FUNCTION auto_increment_orderID();

--Update Order Status
CREATE OR REPLACE FUNCTION update_order_status_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.orderStatus != NEW.orderStatus THEN
        NEW.orderTimestamp := NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_order_status_timestamp_trigger
BEFORE UPDATE ON FoodOrder
FOR EACH ROW
EXECUTE FUNCTION update_order_status_timestamp();

--See Order History
CREATE OR REPLACE FUNCTION update_recent_orders()
RETURNS TRIGGER AS $$
BEGIN
    -- Insert into recentOrders
    INSERT INTO recentOrders (userID, orderID, orderTimestamp)
    VALUES (NEW.userID, NEW.orderID, NEW.orderTimestamp);

    -- Keep only the 5 most recent orders
    DELETE FROM recentOrders
    WHERE userID = NEW.userID
      AND orderTimestamp NOT IN (
          SELECT orderTimestamp
          FROM recentOrders
          WHERE userID = NEW.userID
          ORDER BY orderTimestamp DESC
          LIMIT 5
      );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_recent_orders_trigger
AFTER INSERT ON FoodOrder
FOR EACH ROW
EXECUTE FUNCTION update_recent_orders();



--If Exists Checks
    --Store
CREATE OR REPLACE FUNCTION validate_store_on_order()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Store WHERE storeID = NEW.storeID) THEN
        RAISE EXCEPTION 'Invalid store ID.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_store_on_order_trigger
BEFORE INSERT ON FoodOrder
FOR EACH ROW
EXECUTE FUNCTION validate_store_on_order();

    --Menu
CREATE OR REPLACE FUNCTION prevent_unavailable_items()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Menu WHERE itemName = NEW.itemName AND available = 1) THEN
        RAISE EXCEPTION 'Item not available.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_unavailable_items_trigger
BEFORE INSERT ON ItemsInOrder
FOR EACH ROW
EXECUTE FUNCTION prevent_unavailable_items();



