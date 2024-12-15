import { createStore, combineReducers } from 'redux';
import { persistReducer, persistStore } from 'redux-persist';
import storage from 'redux-persist/lib/storage'; // defaults to localStorage for web

// User reducer
const userReducer = (state = '', action) => {
    switch (action.type) {
        case 'SET_USER':
            return action.payload; // assuming payload is a number representing the user
        case 'LOGOUT':
            return '';
        default:
            return state;
    }
};

// Role reducer
const roleReducer = (state = 'USER', action) => {
    switch (action.type) {
        case 'SET_ROLE':
          return "USER" //return action.payload.ROLE; // assuming payload is a string representing the user's role
        case 'LOGOUT':
            return 'USER';
        default:
            return state;
    }
};

const rootReducer = combineReducers({
    user: userReducer,
    role: roleReducer,
});

const persistConfig = {
    key: 'root',
    storage,
};

const persistedReducer = persistReducer(persistConfig, rootReducer);

export const store = createStore(persistedReducer);
export const persistor = persistStore(store);
