// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';
let currentUser = null;
let jwtToken = null;

// DOM Elements
const loginBtn = document.getElementById('loginBtn');
const logoutBtn = document.getElementById('logoutBtn');
const userWelcome = document.getElementById('userWelcome');
const loginModal = document.getElementById('loginModal');
const productModal = document.getElementById('productModal');
const loginForm = document.getElementById('loginForm');
const productForm = document.getElementById('productForm');
const productsGrid = document.getElementById('productsGrid');
const adminProductsGrid = document.getElementById('adminProductsGrid');
const adminPanel = document.getElementById('adminPanel');
const adminPanelBtn = document.getElementById('adminPanelBtn');
const addProductBtn = document.getElementById('addProductBtn');
const categoryFilter = document.getElementById('categoryFilter');
const searchInput = document.getElementById('searchInput');

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    // Check if user is already logged in
    const savedToken = localStorage.getItem('jwtToken');
    if (savedToken) {
        jwtToken = savedToken;
        validateTokenAndLoadUser();
    }

    // Load initial data
    loadProducts();
    loadCategories();

    // Event Listeners
    setupEventListeners();
}

function setupEventListeners() {
    // Authentication
    loginBtn.addEventListener('click', showLoginModal);
    logoutBtn.addEventListener('click', logout);
    loginForm.addEventListener('submit', handleLogin);

    // Modals
    document.querySelectorAll('.close').forEach(closeBtn => {
        closeBtn.addEventListener('click', closeModals);
    });

    // Product Management
    addProductBtn.addEventListener('click', showAddProductModal);
    productForm.addEventListener('submit', handleProductSubmit);
    document.getElementById('cancelProductBtn').addEventListener('click', closeModals);

    // Navigation
    adminPanelBtn.addEventListener('click', showAdminPanel);
    document.getElementById('viewProductsBtn').addEventListener('click', showPublicProducts);

    // Filters
    categoryFilter.addEventListener('change', filterProducts);
    searchInput.addEventListener('input', debounce(filterProducts, 300));

    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target === loginModal) {
            closeModals();
        }
        if (event.target === productModal) {
            closeModals();
        }
    });
}

// Authentication Functions
async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok && data.token) {
            jwtToken = data.token;
            localStorage.setItem('jwtToken', jwtToken);
            currentUser = data.username;
            updateUIForLoggedInUser();
            closeModals();
            showMessage('Login successful!', 'success');
        } else {
            showMessage(data.message || 'Login failed', 'error');
        }
    } catch (error) {
        showMessage('Error during login: ' + error.message, 'error');
    }
}

async function validateTokenAndLoadUser() {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/validate?token=${jwtToken}`);
        if (response.ok) {
            // Token is valid, get user info
            const userResponse = await fetch(`${API_BASE_URL}/admin/products`, {
                headers: {
                    'Authorization': `Bearer ${jwtToken}`
                }
            });
            if (userResponse.ok) {
                currentUser = 'admin'; // Since we only have admin users
                updateUIForLoggedInUser();
            }
        }
    } catch (error) {
        console.error('Token validation failed:', error);
        logout();
    }
}

function logout() {
    jwtToken = null;
    currentUser = null;
    localStorage.removeItem('jwtToken');
    updateUIForLoggedOutUser();
    showPublicProducts();
    showMessage('Logged out successfully', 'success');
}

function updateUIForLoggedInUser() {
    loginBtn.style.display = 'none';
    logoutBtn.style.display = 'inline-block';
    userWelcome.textContent = `Welcome, ${currentUser}!`;
    userWelcome.style.display = 'inline-block';
    adminPanelBtn.style.display = 'inline-block';
}

function updateUIForLoggedOutUser() {
    loginBtn.style.display = 'inline-block';
    logoutBtn.style.display = 'none';
    userWelcome.style.display = 'none';
    adminPanelBtn.style.display = 'none';
    adminPanel.style.display = 'none';
}

// Modal Functions
function showLoginModal() {
    loginModal.style.display = 'block';
    document.getElementById('loginMessage').innerHTML = '';
}

function showAddProductModal() {
    document.getElementById('productModalTitle').textContent = 'Add New Product';
    document.getElementById('productForm').reset();
    document.getElementById('productId').value = '';
    productModal.style.display = 'block';
}

function showEditProductModal(product) {
    document.getElementById('productModalTitle').textContent = 'Edit Product';
    document.getElementById('productId').value = product.id;
    document.getElementById('productName').value = product.name;
    document.getElementById('productDescription').value = product.description || '';
    document.getElementById('productCategory').value = product.category;
    document.getElementById('productPrice').value = product.price;
    document.getElementById('productQuantity').value = product.quantity;
    document.getElementById('productImage').value = product.imageUrl || '';
    document.getElementById('productAvailable').checked = product.available;
    productModal.style.display = 'block';
}

function closeModals() {
    loginModal.style.display = 'none';
    productModal.style.display = 'none';
}

// Product Management Functions
async function loadProducts() {
    showLoading(true);
    try {
        const response = await fetch(`${API_BASE_URL}/public/products`);
        if (response.ok) {
            const products = await response.json();
            displayProducts(products, productsGrid);
            if (currentUser) {
                loadAdminProducts();
            }
        } else {
            throw new Error('Failed to load products');
        }
    } catch (error) {
        showMessage('Error loading products: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

async function loadAdminProducts() {
    if (!jwtToken) return;

    try {
        const response = await fetch(`${API_BASE_URL}/admin/products`, {
            headers: {
                'Authorization': `Bearer ${jwtToken}`
            }
        });
        if (response.ok) {
            const products = await response.json();
            displayAdminProducts(products);
        }
    } catch (error) {
        console.error('Error loading admin products:', error);
    }
}

async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE_URL}/public/products/categories`);
        if (response.ok) {
            const categories = await response.json();
            populateCategoryFilter(categories);
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

function populateCategoryFilter(categories) {
    categoryFilter.innerHTML = '<option value="">All Categories</option>';
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category;
        option.textContent = category;
        categoryFilter.appendChild(option);
    });
}

function displayProducts(products, container) {
    container.innerHTML = '';

    if (products.length === 0) {
        container.innerHTML = '<div class="no-products">No products found</div>';
        return;
    }

    products.forEach(product => {
        const productCard = createProductCard(product, false);
        container.appendChild(productCard);
    });
}

function displayAdminProducts(products) {
    adminProductsGrid.innerHTML = '';

    if (products.length === 0) {
        adminProductsGrid.innerHTML = '<div class="no-products">No products found</div>';
        return;
    }

    products.forEach(product => {
        const productCard = createProductCard(product, true);
        adminProductsGrid.appendChild(productCard);
    });
}

function createProductCard(product, isAdmin) {
    const card = document.createElement('div');
    card.className = 'product-card';

    const imageContent = product.imageUrl
        ? `<img src="${product.imageUrl}" alt="${product.name}" onerror="this.style.display='none'">`
        : `<i class="fas fa-box"></i>`;

    card.innerHTML = `
        <div class="product-image">
            ${imageContent}
        </div>
        <div class="product-info">
            <div class="product-category">${product.category}</div>
            <h3 class="product-name">${product.name}</h3>
            <p class="product-description">${product.description || 'No description available'}</p>
            <div class="product-meta">
                <div class="product-price">$${product.price}</div>
                <div class="product-quantity">Stock: ${product.quantity}</div>
            </div>
            ${!product.available ? '<div class="out-of-stock">Out of Stock</div>' : ''}
            ${isAdmin ? `
                <div class="product-actions">
                    <button class="btn btn-primary btn-small" onclick="editProduct(${product.id})">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button class="btn btn-outline btn-small" onclick="deleteProduct(${product.id})">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </div>
            ` : ''}
        </div>
    `;

    return card;
}

async function handleProductSubmit(e) {
    e.preventDefault();

    if (!jwtToken) {
        showMessage('Please login first', 'error');
        return;
    }

    const productData = {
        name: document.getElementById('productName').value,
        description: document.getElementById('productDescription').value,
        category: document.getElementById('productCategory').value,
        price: parseFloat(document.getElementById('productPrice').value),
        quantity: parseInt(document.getElementById('productQuantity').value),
        available: document.getElementById('productAvailable').checked,
        imageUrl: document.getElementById('productImage').value || null
    };

    const productId = document.getElementById('productId').value;
    const isEdit = !!productId;

    try {
        const url = isEdit
            ? `${API_BASE_URL}/admin/products/${productId}`
            : `${API_BASE_URL}/admin/products`;

        const response = await fetch(url, {
            method: isEdit ? 'PUT' : 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${jwtToken}`
            },
            body: JSON.stringify(productData)
        });

        if (response.ok) {
            closeModals();
            showMessage(`Product ${isEdit ? 'updated' : 'created'} successfully!`, 'success');
            loadProducts();
            loadAdminProducts();
        } else {
            const error = await response.text();
            throw new Error(error);
        }
    } catch (error) {
        showMessage('Error saving product: ' + error.message, 'error');
    }
}

async function editProduct(productId) {
    try {
        const response = await fetch(`${API_BASE_URL}/public/products/${productId}`);
        if (response.ok) {
            const product = await response.json();
            showEditProductModal(product);
        } else {
            throw new Error('Product not found');
        }
    } catch (error) {
        showMessage('Error loading product: ' + error.message, 'error');
    }
}

async function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product?')) {
        return;
    }

    if (!jwtToken) {
        showMessage('Please login first', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/admin/products/${productId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${jwtToken}`
            }
        });

        if (response.ok) {
            showMessage('Product deleted successfully!', 'success');
            loadProducts();
            loadAdminProducts();
        } else {
            throw new Error('Failed to delete product');
        }
    } catch (error) {
        showMessage('Error deleting product: ' + error.message, 'error');
    }
}

// Filter and Search Functions
function filterProducts() {
    const searchTerm = searchInput.value.toLowerCase();
    const selectedCategory = categoryFilter.value;

    const productCards = productsGrid.querySelectorAll('.product-card');

    productCards.forEach(card => {
        const productName = card.querySelector('.product-name').textContent.toLowerCase();
        const productCategory = card.querySelector('.product-category').textContent;
        const productDescription = card.querySelector('.product-description').textContent.toLowerCase();

        const matchesSearch = productName.includes(searchTerm) || productDescription.includes(searchTerm);
        const matchesCategory = !selectedCategory || productCategory === selectedCategory;

        card.style.display = matchesSearch && matchesCategory ? 'block' : 'none';
    });
}

// Utility Functions
function showLoading(show) {
    const loading = document.getElementById('loading');
    loading.style.display = show ? 'block' : 'none';
}

function showMessage(message, type) {
    // Create or find message container
    let messageDiv = document.getElementById('globalMessage');
    if (!messageDiv) {
        messageDiv = document.createElement('div');
        messageDiv.id = 'globalMessage';
        messageDiv.style.cssText = `
            position: fixed;
            top: 100px;
            right: 20px;
            z-index: 3000;
            min-width: 300px;
            padding: 1rem;
            border-radius: 5px;
            color: white;
            font-weight: 500;
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        `;
        document.body.appendChild(messageDiv);
    }

    messageDiv.textContent = message;
    messageDiv.className = `message ${type}`;
    messageDiv.style.backgroundColor = type === 'success' ? '#28a745' : '#dc3545';
    messageDiv.style.display = 'block';

    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 3000);
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Navigation Functions
function showAdminPanel() {
    document.getElementById('products').scrollIntoView({ behavior: 'smooth' });
    adminPanel.style.display = 'block';
    loadAdminProducts();
}

function showPublicProducts() {
    adminPanel.style.display = 'none';
    document.getElementById('products').scrollIntoView({ behavior: 'smooth' });
}

// Add some CSS for the global message
const style = document.createElement('style');
style.textContent = `
    .btn-small {
        padding: 6px 12px;
        font-size: 0.8rem;
    }
    
    .out-of-stock {
        background: #ff6b6b;
        color: white;
        padding: 4px 8px;
        border-radius: 3px;
        font-size: 0.8rem;
        display: inline-block;
        margin-bottom: 1rem;
    }
    
    .no-products {
        text-align: center;
        padding: 3rem;
        color: #666;
        font-size: 1.1rem;
        grid-column: 1 / -1;
    }
`;
document.head.appendChild(style);